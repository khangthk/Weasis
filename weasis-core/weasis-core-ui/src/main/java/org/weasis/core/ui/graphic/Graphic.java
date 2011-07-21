/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.core.ui.graphic;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.weasis.core.api.gui.util.GUIEntry;
import org.weasis.core.api.media.data.ImageElement;
import org.weasis.core.ui.editor.image.DefaultView2d;

/**
 * The Interface Graphic.
 * 
 * @author Nicolas Roduit , Benoit Jacquemoud
 */
public interface Graphic extends GUIEntry , Cloneable {

    public Graphic deepCopy();
  
    public Area getArea(AffineTransform transform); 

    public String[] getLabel();

    public void setLabel(String[] label, DefaultView2d view2d);

    public void setSelected(boolean flag);

    public boolean isSelected();

    public void paint(Graphics2D g2, AffineTransform transform);

    public void paintLabel(Graphics2D g2, AffineTransform transform);

    public void updateLabel(Object source, DefaultView2d view2d);

    public Rectangle getBounds(AffineTransform transform); 

    public Rectangle getRepaintBounds(AffineTransform transform); 

    public GraphicLabel getGraphicLabel();

    public boolean isLabelVisible();

    public boolean intersects(Rectangle rectangle, AffineTransform transform); 

    public void addPropertyChangeListener(PropertyChangeListener propertychangelistener);

    public void removePropertyChangeListener(PropertyChangeListener propertychangelistener);

    public void fireRemoveAction();

    public void toFront();

    public void toBack();

    public void setLayerID(int layerID);

    public int getLayerID();

    public Shape getShape();

    public Rectangle getTransformedBounds(Shape shape, AffineTransform transform);

    public Rectangle getTransformedBounds(GraphicLabel label, AffineTransform transform);

    public List<MeasureItem> getMeasurements(ImageElement imageElement, boolean releaseEvent, boolean drawOnLabel);
}
