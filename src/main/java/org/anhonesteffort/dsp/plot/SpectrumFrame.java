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

package org.anhonesteffort.dsp.plot;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.anhonesteffort.dsp.dft.DftFrame;
import org.anhonesteffort.dsp.dft.DftToDecibelConverter;
import org.anhonesteffort.dsp.dft.SamplesToDftConverter;
import org.anhonesteffort.dsp.filter.Filter;
import org.anhonesteffort.dsp.sample.DynamicSink;
import org.anhonesteffort.dsp.sample.Samples;
import org.anhonesteffort.dsp.dft.DftWidth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;

public class SpectrumFrame extends JFrame
    implements DynamicSink<Samples>, FutureCallback<Void>, ComponentListener, WindowListener {

  private static final Logger log = LoggerFactory.getLogger(SpectrumFrame.class);

  private final SpectrumPanel            spectrumPanel;
  private final SpectrumGridOverlayPanel gridPanel;
  private final SamplesToDftConverter    dftConverter;
  private final ListenableFuture         dftConverterFuture;

  public SpectrumFrame(ExecutorService executor,
                       DftWidth        dftWidth,
                       Integer         averaging,
                       Integer         frameRate,
                       Integer         samplesQueueSize)
  {
    super("DFT Plot");

    setLayout(new BorderLayout());
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    spectrumPanel = new SpectrumPanel(averaging, frameRate);
    gridPanel     = new SpectrumGridOverlayPanel();

    getLayeredPane().add(spectrumPanel, 0, 0);
    getLayeredPane().add(gridPanel,     1, 0);

    addWindowListener(this);
    addWindowListener(spectrumPanel);
    getLayeredPane().addComponentListener(this);

                     dftConverter     = new SamplesToDftConverter(dftWidth, samplesQueueSize);
    Filter<DftFrame> decibelConverter = new DftToDecibelConverter();

    dftConverter.addSink(decibelConverter);
    decibelConverter.addSink(spectrumPanel);

    dftConverterFuture = MoreExecutors.listeningDecorator(executor).submit(dftConverter);
    Futures.addCallback(dftConverterFuture, this);
  }

  @Override
  public void onSourceStateChange(Long sampleRate, Double frequency) {
    gridPanel.onSourceStateChange(sampleRate, frequency);
  }

  @Override
  public void consume(Samples element) {
    dftConverter.consume(element);
  }

  @Override
  public void componentResized(ComponentEvent e) {
    int width  = e.getComponent().getWidth();
    int height = e.getComponent().getHeight();

    spectrumPanel.setBounds(0, 0, width, height);
    gridPanel.setBounds(0, 0, width, height);
  }

  @Override
  public void windowClosing(WindowEvent e) {
    dftConverterFuture.cancel(true);
  }

  @Override
  public void onSuccess(Void nothing) {
    log.error("dft converter stopped unexpectedly, closing frame");
    dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
  }

  @Override
  public void onFailure(Throwable throwable) {
    if (!(throwable instanceof CancellationException)) {
      log.error("dft converter stopped unexpectedly, closing frame", throwable);
    }
    dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
  }

  @Override
  public void componentMoved(ComponentEvent e) { }

  @Override
  public void componentShown(ComponentEvent e) { }

  @Override
  public void componentHidden(ComponentEvent e) { }

  @Override
  public void windowOpened(WindowEvent e) { }

  @Override
  public void windowClosed(WindowEvent e) { }

  @Override
  public void windowIconified(WindowEvent e) { }

  @Override
  public void windowDeiconified(WindowEvent e) { }

  @Override
  public void windowActivated(WindowEvent e) { }

  @Override
  public void windowDeactivated(WindowEvent e) { }

}
