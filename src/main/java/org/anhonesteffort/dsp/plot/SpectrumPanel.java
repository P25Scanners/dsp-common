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

import org.anhonesteffort.dsp.Sink;
import org.anhonesteffort.dsp.dft.DftFrame;
import org.anhonesteffort.dsp.util.CircularFloatAveragingQueue;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.GeneralPath;
import java.util.Optional;

public class SpectrumPanel extends JPanel implements Sink<DftFrame>, ActionListener, WindowListener {

  private static final Color          BACKGROUND_COLOR = Color.BLACK;
  private static final Color          SPECTRUM_COLOR   = Color.RED;
  private static final RenderingHints RENDERING_HINTS  = new RenderingHints(null);

  static {
    RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    RENDERING_HINTS.put(RenderingHints.KEY_RENDERING,    RenderingHints.VALUE_RENDER_QUALITY);
  }

  private final Timer                       timer;
  private final CircularFloatAveragingQueue decibelQueue;
  private final Object                      queueLock = new Object();

  private int dftWidth = -1;

  public SpectrumPanel(int averaging, int frameRate) {
    timer        = new Timer(1000 / frameRate, this);
    decibelQueue = new CircularFloatAveragingQueue(averaging);

    timer.start();
  }

  @Override
  public void consume(DftFrame element) {
    synchronized (queueLock) {
      if (dftWidth != element.getFrame().length) {
        decibelQueue.clear();
        dftWidth = element.getFrame().length;
      }

      decibelQueue.add(element.getFrame());
    }
  }

  private void handlePaintBackground(Graphics2D graphics, Dimension size) {
    Rectangle background = new Rectangle(0, 0, size.width, size.height);
    graphics.setColor(BACKGROUND_COLOR);
    graphics.draw(background);
    graphics.fill(background);
  }

  private void handlePaintSpectrum(Graphics2D graphics, Dimension size, float[] currentDft) {
    float       scalar       = 3.5f;
    float       binWidth     = (float) size.getWidth() / currentDft.length;
    GeneralPath spectrumPath = new GeneralPath();

    spectrumPath.moveTo(size.getWidth(), size.getHeight());
    spectrumPath.lineTo(0, size.getHeight());

    for(int binIndex = 0; binIndex < currentDft.length; binIndex++) {
      float height;

      if(currentDft[binIndex] >= 0f) {
        height = 0;
      } else {
        height = Math.abs(currentDft[binIndex] * scalar);
        if(height > size.height)
          height = size.height;
      }

      spectrumPath.lineTo((binIndex * binWidth), height);
    }

    spectrumPath.lineTo(size.getWidth(), size.getHeight());

    graphics.setPaint(SPECTRUM_COLOR);
    graphics.draw(spectrumPath);
    graphics.fill(spectrumPath);
  }

  private Optional<float[]> getCurrentDft() {
    synchronized (queueLock) {
      if (!decibelQueue.isEmpty())
        return Optional.of(decibelQueue.remove());
    }

    return Optional.empty();
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D graphics = (Graphics2D) g;
    Dimension  size     = getSize();

    graphics.setRenderingHints(RENDERING_HINTS);
    handlePaintBackground(graphics, size);

    Optional<float[]> currentDft = getCurrentDft();
    if (currentDft.isPresent())
      handlePaintSpectrum(graphics, size, currentDft.get());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!decibelQueue.isEmpty())
      repaint();
  }

  @Override
  public void windowClosing(WindowEvent e) {
    timer.stop();
    synchronized (queueLock) { decibelQueue.clear(); }
  }

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
