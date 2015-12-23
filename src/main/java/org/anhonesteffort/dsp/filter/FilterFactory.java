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

package org.anhonesteffort.dsp.filter;

import org.anhonesteffort.dsp.ComplexNumber;
import org.anhonesteffort.dsp.filter.rate.ComplexNumberFirstOrderCicDecimatingFilter;
import org.anhonesteffort.dsp.filter.rate.ComplexNumberFirstOrderCicInterpolatingFilter;
import org.anhonesteffort.dsp.filter.rate.ComplexNumberResamplingFilter;
import org.anhonesteffort.dsp.filter.rate.RateChangeFilter;

public class FilterFactory {

  public static ComplexNumberFirFilter getKaiserBessel(long  sampleRate,
                                                       long  passbandStop,
                                                       long  stopbandStart,
                                                       int   attenuation,
                                                       float gain)
  {
    final long    transitionWidth  = stopbandStart - passbandStop;
    final long    transFreq        = passbandStop + (transitionWidth / 2);
    final int     filterLength     = FilterUtil.getKaiserBesselFilterLength(sampleRate, transitionWidth, attenuation);
    final float[] idealResponse    = FilterUtil.getLowPassIdealImpulseResponse(sampleRate, transFreq, filterLength);
    final float   windowShape      = FilterUtil.getKaiserBesselWindowShape(attenuation);
    final float[] windowedResponse = FilterUtil.getKaiserBesselWindow(idealResponse, windowShape);

    return new ComplexNumberFirFilter(windowedResponse, gain);
  }

  public static Filter<ComplexNumber> getCicCompensation(long channelRate,
                                                         long passbandStop,
                                                         int  stageCount)
  {
    /**
     * both decimate and interpolate filters are first order so we get ~16db
     * alias/image rejection with a ~1db passband droop. we can increase the
     * number of filter stages to improve alias/image rejection but this also
     * increases passband droop.
     *
     * todo: compensate for passband droop, increase cic stage count
     */
    return new ComplexNumberFirFilter(new float[] {1}, 1f);
  }

  public static RateChangeFilter<ComplexNumber> getCicDecimate(long sourceRate, long channelRate) {
    if (sourceRate <= channelRate)
      throw new IllegalArgumentException("source rate must be greater than channel rate");

    int decimation = (int) (sourceRate / channelRate);
    return new ComplexNumberFirstOrderCicDecimatingFilter(decimation);
  }

  public static RateChangeFilter<ComplexNumber> getCicInterpolate(long sourceRate, long channelRate) {
    if (sourceRate >= channelRate)
      throw new IllegalArgumentException("channel rate must be greater than source rate");

    int interpolation = (int) (channelRate / sourceRate);
    return new ComplexNumberFirstOrderCicInterpolatingFilter(interpolation);
  }

  public static RateChangeFilter<ComplexNumber> getCicResampler(long sourceRate,
                                                                long channelRate,
                                                                long maxRateDiff)
  {
    int[] factors = FilterUtil.getInterpolationAndDecimation(sourceRate, channelRate, maxRateDiff);

    RateChangeFilter<ComplexNumber> interpolation =
        new ComplexNumberFirstOrderCicInterpolatingFilter(factors[0]);

    RateChangeFilter<ComplexNumber> decimation =
        new ComplexNumberFirstOrderCicDecimatingFilter(factors[1]);

    return new ComplexNumberResamplingFilter(interpolation, decimation);
  }

}
