/*
 * Copyright (C) 2015 An Honest Effort LLC, coping.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.anhonesteffort.dsp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class ConcurrentSource<T extends Copyable<T>, L extends Sink<T>> extends Source<T, L> {

  private static final ExecutorService executor = Executors.newCachedThreadPool();
  private Future producer = null;

  public static void shutdownSources() {
    executor.shutdownNow();
  }

  protected abstract Runnable newProducer();

  protected boolean isProducing() {
    return producer != null && !producer.isDone();
  }

  private void startProducing() {
    assert !executor.isShutdown() && (producer == null || producer.isDone());
    producer = executor.submit(newProducer());
  }

  private void stopProducing() {
    assert producer != null;
    producer.cancel(true);
  }

  @Override
  public void addSink(L sink) {
    synchronized (sinks) {
      if (sinks.isEmpty())
        startProducing();
      sinks.add(sink);
    }
  }

  @Override
  public void removeSink(L sink) {
    synchronized (sinks) {
      sinks.remove(sink);
      if (sinks.isEmpty())
        stopProducing();
    }
  }

}