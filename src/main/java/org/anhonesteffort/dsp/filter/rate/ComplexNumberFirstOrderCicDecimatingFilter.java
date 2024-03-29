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

package org.anhonesteffort.dsp.filter.rate;

import org.anhonesteffort.dsp.ComplexNumber;
import org.anhonesteffort.dsp.Sink;
import org.anhonesteffort.dsp.filter.Filter;

import java.util.stream.IntStream;

public class ComplexNumberFirstOrderCicDecimatingFilter extends RateChangeFilter<ComplexNumber> {

  private final Filter<ComplexNumber> firstStage;

  public ComplexNumberFirstOrderCicDecimatingFilter(int decimation) {
    super(1, decimation);
    firstStage = new DecimatingCicStage(decimation, 1);
  }

  @Override
  public void consume(ComplexNumber element) {
    firstStage.consume(element);
  }

  @Override
  public void addSink(Sink<ComplexNumber> sink) {
    firstStage.addSink(sink);
  }

  @Override
  public void removeSink(Sink<ComplexNumber> sink) {
    firstStage.removeSink(sink);
  }

  public static class DecimatingCicStage extends Filter<ComplexNumber> {

    private final ComplexNumber[] delayLine;
    private final int             decimation;
    private final int             diffDelay;
    private final float           gain;

    private ComplexNumber last       = new ComplexNumber(0, 0);
    private int           delayIndex = 0;

    public DecimatingCicStage(int decimation, int diffDelay) {
      this.decimation = decimation;
      this.diffDelay  = diffDelay;
      gain            = 1f / decimation;
      delayLine       = new ComplexNumber[decimation * diffDelay];

      IntStream.range(0, delayLine.length).forEach(i -> delayLine[i] = last);
    }

    private ComplexNumber integrate(ComplexNumber element) {
      last = element.add(last);
      return last;
    }

    private void comb(ComplexNumber element) {
      ComplexNumber output  = element.subtract(delayLine[delayIndex]);
      delayLine[delayIndex] = element;
      delayIndex            = (delayIndex + 1) % (decimation * diffDelay);

      if (delayIndex == 0)
        broadcast(output.multiply(gain));
    }

    @Override
    public void consume(ComplexNumber element) {
      comb(integrate(element));
    }

  }

}
