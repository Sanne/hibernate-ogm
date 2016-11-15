/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.ogm.datastore.infinispan.test.stress;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.ogm.datastore.infinispan.InfinispanDialect;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.TestingUtil;
import org.junit.Test;

/**
 * Checks for unique generation of ids on a specific Infinispan
 * configuration.
 * This test focuses on "coordinated execution": all threads will
 * be set to pause just before the critical point, then released
 * together, then block again for verification.. rinse and repeat.
 *
 * @author Sanne Grinovero
 */
public class OgmCounterStressTest {

	private static final int MOVES = 20_000;
	private static final int THREADS = 12;
	private static final String SHARED_KEY = "thisIsTheKeyForConcurrentAccess";
	private static final String CACHE_NAME = "hibernate_sequences";

	private static final AtomicBoolean failed = new AtomicBoolean( false );
	private static final AtomicBoolean quit = new AtomicBoolean( false );
	private static final AtomicInteger liveWorkers = new AtomicInteger();
	private static volatile String failureMessage = "";

	/**
	 * Testing atomic sequence increment on LOCAL caches with no transactions.
	 *
	 * @throws Exception
	 */
	@Test
	public void testOnLocal() throws Exception {
		final EmbeddedCacheManager cacheManager = new DefaultCacheManager( "infinispan-local.xml"  );
		try {
			testOnCaches( cacheManager );
		}
		finally {
			TestingUtil.killCacheManagers( cacheManager );
		}
	}

	private void testOnCaches(EmbeddedCacheManager cacheManager) {
		failed.set( false );
		quit.set( false );
		final SharedState state = new SharedState( THREADS );
		final PostOperationStateCheck stateCheck = new PostOperationStateCheck( cacheManager, state );
		final CyclicBarrier barrier = new CyclicBarrier( THREADS, stateCheck );
		ExecutorService exec = Executors.newFixedThreadPool( THREADS );
		AdvancedCache<Object,Object> cache = cacheManager.getCache( CACHE_NAME ).getAdvancedCache();
		for ( int threadIndex = 0; threadIndex < THREADS; threadIndex++ ) {
			Runnable validMover = new ValidMover( cache, barrier, threadIndex, state );
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

		private final AdvancedCache cache;
		private final int threadIndex;
		private final CyclicBarrier barrier;
		private final SharedState state;

		public ValidMover(Cache cache, CyclicBarrier barrier, int threadIndex, SharedState state) {
			this.cache = cache.getAdvancedCache();
			this.barrier = barrier;
			this.threadIndex = threadIndex;
			this.state = state;
		}

		@Override
		public void run() {
			liveWorkers.incrementAndGet();
			try {
				for ( int movesDone = 0; movesDone < MOVES && !barrier.isBroken() && !failed.get() && !quit.get(); movesDone++ ) {
					state.beforeEvent();
					barrier.await();
					Number newValue = InfinispanDialect.incrementCounter( cache, SHARED_KEY, 0, 1 );
					state.afterReplace( threadIndex, newValue );
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

		private final long[] generatedValuePerThread;
		private boolean after = false;

		public SharedState(final int threads) {
			generatedValuePerThread = new long[threads];
		}

		public synchronized void beforeEvent() {
			after = false;
		}

		public synchronized void afterReplace(int threadIndex, Number newValue) {
			generatedValuePerThread[threadIndex] = newValue.longValue();
			after = true;
		}

		synchronized boolean isAfter() {
			return after;
		}

	}

	static final class PostOperationStateCheck implements Runnable {

		private final Cache cache;
		private final SharedState state;
		private final AtomicInteger cycle = new AtomicInteger();
		private final HashSet allGenerated = new HashSet();

		public PostOperationStateCheck(final EmbeddedCacheManager cacheManager, final SharedState state) {
			this.cache = cacheManager.getCache( CACHE_NAME );
			this.state = state;
		}

		@Override
		public void run() {
			if ( state.isAfter() ) {
				int c = cycle.incrementAndGet();
				if ( c % ( MOVES / 100 ) == 0 ) {
					System.out.println( ( c * 100 / MOVES ) + "%" );
				}
				checkAfterState();
			}
			else {
				checkBeforeState();
			}
		}

		private void checkBeforeState() {
			//nothing
		}

		private void checkAfterState() {
			HashSet lastIterationGenerated = new HashSet();
			for ( long v : state.generatedValuePerThread ) {
				lastIterationGenerated.add( v );
				allGenerated.add( v );
			}
			assert lastIterationGenerated.size() == THREADS : "Apparently didn't generate unique values!";
			assert allGenerated.size() == (THREADS * cycle.get()) : "Overlap of generated values with previous generation!";
		}
	}

}
