/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispan.test.stress;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.TestingUtil;
import org.infinispan.util.concurrent.locks.LockManager;
import org.junit.Test;

/**
 * Stress test to verify the Infinispan configuration file to be
 * suited for CAS operations, as we use them for sequence generation.
 *
 * The Infinispan API allows to invoke such CAS operations on any
 * Cache configuration, although only some configurations can
 * guarantee the atomic semantics which we rely on in our sequence
 * generation code: we use this tool to validate our own test
 * configuration and understanding of the Infinispan configuration
 * settings.
 *
 * @author Sanne Grinovero
 */
public class ReplaceOperationStressTest {

	private static final int NODES_NUM = 5;
	private static final int MOVES = 200_000;
	private static final int THREADS = 10;
	private static final String SHARED_KEY = "thisIsTheKeyForConcurrentAccess";
	private static final String CACHE_NAME = "hibernate_sequences";

	private static final String[] validMoves = generateValidMoves();

	private static final AtomicBoolean failed = new AtomicBoolean( false );
	private static final AtomicBoolean quit = new AtomicBoolean( false );
	private static final AtomicInteger liveWorkers = new AtomicInteger();
	private static volatile String failureMessage = "";

	private static String[] generateValidMoves() {
		String[] validMoves = new String[MOVES];
		for ( int i = 0; i < MOVES; i++ ) {
			validMoves[i] = "v_" + i;
		}
		System.out.println( "Valid moves ready" );
		return validMoves;
	}

	/**
	 * Testing replace(Object, Object, Object) behaviour on LOCAL caches with no transactions.
	 *
	 * @throws Exception
	 */
	@Test
	public void testOnLocal() throws Exception {
		testonInfinispanConiguration( "infinispan-local.xml" );
	}

	public void testonInfinispanConiguration(String cfgFile) throws Exception {
		final List<EmbeddedCacheManager> cacheManagers = new ArrayList<EmbeddedCacheManager>( NODES_NUM );
		final List<Cache> caches = new ArrayList<Cache>( NODES_NUM );
		final EmbeddedCacheManager firstCacheManager = new DefaultCacheManager( cfgFile );
		final Cache firstCache = firstCacheManager.getCache( CACHE_NAME );
		final CacheMode cacheMode = firstCache.getCacheConfiguration().clustering().cacheMode();
		cacheManagers.add( firstCacheManager );
		caches.add( firstCache );

		if ( cacheMode.isClustered() ) {
			for ( int i = 1; i < NODES_NUM; i++ ) {
				EmbeddedCacheManager cacheManager = new DefaultCacheManager( cfgFile );
				cacheManagers.add( cacheManager );
				caches.add( cacheManager.getCache( CACHE_NAME ) );
			}
			TestingUtil.blockUntilViewsReceived( 10000, caches );
		}
		if ( cacheMode.isDistributed() ) {
			TestingUtil.waitForRehashToComplete( caches );
		}
		try {
			testOnCaches( caches );
		}
		finally {
			TestingUtil.killCaches( caches );
			TestingUtil.killCacheManagers( cacheManagers );
		}
	}

	private void testOnCaches(List<Cache> caches) {
		failed.set( false );
		quit.set( false );
		caches.get( 0 ).put( SHARED_KEY, validMoves[0] );
		final SharedState state = new SharedState( THREADS );
		final PostOperationStateCheck stateCheck = new PostOperationStateCheck( caches, state );
		final CyclicBarrier barrier = new CyclicBarrier( THREADS, stateCheck );
		ExecutorService exec = Executors.newFixedThreadPool( THREADS );
		for ( int threadIndex = 0; threadIndex < THREADS; threadIndex++ ) {
			Runnable validMover = new ValidMover( caches, barrier, threadIndex, state );
			exec.execute( validMover );
		}
		exec.shutdown();
		try {
			exec.awaitTermination( 1, TimeUnit.DAYS );
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			assert false : e.getMessage();
		}
		assert !failed.get() : failureMessage;
	}

	private static void fail(final String message) {
		boolean firstFailure = failed.compareAndSet( false, true );
		if ( firstFailure ) {
			failureMessage = message;
		}
	}

	private static void fail(final Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		e.printStackTrace( pw );
		fail( sw.toString() );
	}

	static final class ValidMover implements Runnable {

		private final List<Cache> caches;
		private final int threadIndex;
		private final CyclicBarrier barrier;
		private final SharedState state;

		public ValidMover(List<Cache> caches, CyclicBarrier barrier, int threadIndex, SharedState state) {
			this.caches = caches;
			this.barrier = barrier;
			this.threadIndex = threadIndex;
			this.state = state;
		}

		@Override
		public void run() {
			int cachePickIndex = threadIndex;
			liveWorkers.incrementAndGet();
			try {
				for ( int moveToIndex = threadIndex; ( moveToIndex < validMoves.length )
						&& ( !barrier.isBroken() && ( !failed.get() ) && !quit.get() ); moveToIndex += THREADS ) {
					cachePickIndex = ++cachePickIndex % caches.size();
					Cache cache = caches.get( cachePickIndex );
					Object expected = cache.get( SHARED_KEY );
					String targetValue = validMoves[moveToIndex];
					state.beforeReplace( threadIndex, expected, targetValue );
					barrier.await();
					boolean replaced = cache.replace( SHARED_KEY, expected, targetValue );
					state.afterReplace( threadIndex, expected, targetValue, replaced );
					barrier.await();
				}
				// not all threads might finish at the same block, so make sure noone stays waiting for us when we exit
				quit.set( true );
				barrier.reset();
			}
			catch (InterruptedException e) {
				fail( e );
			}
			catch (BrokenBarrierException e) {
				// just quit
				System.out.println( "Broken barrier!" );
			}
			catch (RuntimeException e) {
				fail( e );
			}
			finally {
				int andGet = liveWorkers.decrementAndGet();
				barrier.reset();
				System.out.println( "Thread #" + threadIndex + " terminating. Still " + andGet + " threads alive" );
			}
		}
	}

	static final class SharedState {

		private final SharedThreadState[] threadstates;
		private boolean after = false;

		public SharedState(final int threads) {
			threadstates = new SharedThreadState[threads];
			for ( int i = 0; i < threads; i++ ) {
				threadstates[i] = new SharedThreadState();
			}
		}

		synchronized void beforeReplace(int threadIndex, Object expected, String targetValue) {
			threadstates[threadIndex].beforeReplace( expected, targetValue );
			after = false;
		}

		synchronized void afterReplace(int threadIndex, Object expected, String targetValue, boolean replace) {
			threadstates[threadIndex].afterReplace( expected, targetValue, replace );
			after = true;
		}

		synchronized boolean isAfter() {
			return after;
		}

	}

	static final class SharedThreadState {

		volatile Object beforeExpected;
		volatile Object beforeTargetValue;
		volatile Object afterExpected;
		volatile Object afterTargetValue;
		volatile boolean successfullyReplaced;

		public void beforeReplace(Object expected, Object targetValue) {
			this.beforeExpected = expected;
			this.beforeTargetValue = targetValue;
		}

		public void afterReplace(Object expected, Object targetValue, boolean replaced) {
			this.afterExpected = expected;
			this.afterTargetValue = targetValue;
			this.successfullyReplaced = replaced;
		}
	}

	static final class PostOperationStateCheck implements Runnable {

		private final List<Cache> caches;
		private final SharedState state;
		private final AtomicInteger cycle = new AtomicInteger();

		public PostOperationStateCheck(final List<Cache> caches, final SharedState state) {
			this.caches = caches;
			this.state = state;
		}

		@Override
		public void run() {
			if ( state.isAfter() ) {
				int c = cycle.incrementAndGet();
				if ( c % ( MOVES / 100 ) == 0 ) {
					System.out.println( ( c * 100 * THREADS / MOVES ) + "%" );
				}
				checkAfterState();
			}
			else {
				checkBeforeState();
			}
		}

		private void checkBeforeState() {
			final Object currentStored = caches.get( 0 ).get( SHARED_KEY );
			for ( Cache c : caches ) {
				if ( !currentStored.equals( c.get( SHARED_KEY ) ) ) {
					fail( "Precondition failure: not all caches are storing the same value" );
				}
			}
			for ( SharedThreadState threadState : state.threadstates ) {
				if ( !threadState.beforeExpected.equals( currentStored ) ) {
					fail( "Some cache expected a different value than what is stored" );
				}
			}
		}

		private void checkAfterState() {
			AdvancedCache someCache = caches.get( 0 ).getAdvancedCache();
			final Object currentStored = someCache.get( SHARED_KEY );
			HashSet uniqueValueVerify = new HashSet();
			for ( SharedThreadState threadState : state.threadstates ) {
				uniqueValueVerify.add( threadState.afterTargetValue );
			}
			if ( uniqueValueVerify.size() != THREADS ) {
				fail( "test bug! Workers aren't attempting to write different values" );
			}
			{
				int replaced = 0;
				for ( SharedThreadState threadState : state.threadstates ) {
					if ( threadState.successfullyReplaced ) {
						replaced++;
					}
				}
				if ( replaced != 1 ) {
					fail( replaced + " threads assume a succesfull replacement! (CAS should succeed on a single thread only)" );
				}
			}
			for ( SharedThreadState threadState : state.threadstates ) {
				if ( threadState.successfullyReplaced ) {
					if ( !threadState.afterTargetValue.equals( currentStored ) ) {
						fail( "replace successful but the current stored value doesn't match the write operation of the successful thread" );
					}
				}
				else {
					if ( threadState.afterTargetValue.equals( currentStored ) ) {
						fail( "replace not successful (which is fine) but the current stored value matches the write attempt" );
					}
				}
			}
			for ( Cache c : caches ) {
				LockManager lockManager = c.getAdvancedCache().getComponentRegistry().getComponent( LockManager.class );
				//TODO: this method is not available in Infinispan 8, enable on newer versions:
				// Unlocking is asynchronous so allow needing to wait for it:
				// eventually( () -> lockManager.isLocked( SHARED_KEY ) == false );
			}
		}
	}

}
