/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.medusa.skins;

import eu.hansolo.medusa.Fonts;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.tools.Helper;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;

import java.util.Locale;


/**
 * Created by hansolo on 06.01.16.
 */
public class FlatSkin extends SkinBase<Gauge> implements Skin<Gauge> {
    private static final double PREFERRED_WIDTH  = 250;
    private static final double PREFERRED_HEIGHT = 250;
    private static final double MINIMUM_WIDTH    = 50;
    private static final double MINIMUM_HEIGHT   = 50;
    private static final double MAXIMUM_WIDTH    = 1024;
    private static final double MAXIMUM_HEIGHT   = 1024;

    private double              size;
    private Circle              colorRing;
    private Arc                 bar;
    private Line                separator;
    private Circle              background;
    private Text                titleText;
    private Text                valueText;
    private Text                unitText;
    private Pane                pane;
    private double              angleStep;


    // ******************** Constructors **************************************
    public FlatSkin(Gauge gauge) {
        super(gauge);
        angleStep = gauge.getAngleStep();

        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getSkinnable().getPrefWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getPrefHeight(), 0.0) <= 0 ||
            Double.compare(getSkinnable().getWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getHeight(), 0.0) <= 0) {
            if (getSkinnable().getPrefWidth() < 0 && getSkinnable().getPrefHeight() < 0) {
                getSkinnable().setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getSkinnable().getMinWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMinHeight(), 0.0) <= 0) {
            getSkinnable().setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getSkinnable().getMaxWidth(), 0.0) <= 0 || Double.compare(getSkinnable().getMaxHeight(), 0.0) <= 0) {
            getSkinnable().setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        colorRing = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.5);
        colorRing.setFill(Color.TRANSPARENT);
        colorRing.setStrokeWidth(PREFERRED_WIDTH * 0.0075);
        colorRing.setStroke(getSkinnable().getBarColor());

        bar = new Arc(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.468, PREFERRED_HEIGHT * 0.468, 90, 0);
        bar.setType(ArcType.OPEN);
        bar.setStroke(getSkinnable().getBarColor());
        bar.setStrokeWidth(PREFERRED_WIDTH * 0.15);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);
        bar.setFill(null);

        separator = new Line(PREFERRED_WIDTH * 0.5, 1, PREFERRED_WIDTH * 0.5, 0.16667 * PREFERRED_HEIGHT);
        separator.setStroke(getSkinnable().getBorderPaint());
        separator.setFill(Color.TRANSPARENT);

        background = new Circle(PREFERRED_WIDTH * 0.5, PREFERRED_HEIGHT * 0.5, PREFERRED_WIDTH * 0.363);
        background.setFill(getSkinnable().getBackgroundPaint());

        titleText = new Text(getSkinnable().getTitle());
        titleText.setFont(Fonts.robotoLight(PREFERRED_WIDTH * 0.08));
        titleText.setFill(getSkinnable().getTitleColor());

        valueText = new Text(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", getSkinnable().getCurrentValue()));
        valueText.setFont(Fonts.robotoRegular(PREFERRED_WIDTH * 0.27333));
        valueText.setFill(getSkinnable().getValueColor());

        unitText = new Text(getSkinnable().getUnit());
        unitText.setFont(Fonts.robotoLight(PREFERRED_WIDTH * 0.08));
        unitText.setFill(getSkinnable().getUnitColor());

        pane = new Pane(colorRing, bar, separator, background, titleText, valueText, unitText);

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        getSkinnable().widthProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().heightProperty().addListener(o -> handleEvents("RESIZE"));
        getSkinnable().setOnUpdate(e -> handleEvents(e.eventType.name()));
        getSkinnable().currentValueProperty().addListener(o -> setBar(getSkinnable().getCurrentValue()));
    }


    // ******************** Methods *******************************************
    private void handleEvents(final String EVENT_TYPE) {
        if ("RESIZE".equals(EVENT_TYPE)) {
            resize();
        } else if ("REDRAW".equals(EVENT_TYPE)) {
            redraw();
        } else if ("VISIBILITY".equals(EVENT_TYPE)) {

        }
    }

    private void setBar(final double VALUE) {
        double minValue = getSkinnable().getMinValue();
        if (getSkinnable().isColorGradientEnabled()) { bar.setFill(getSkinnable().getGradientLookup().getColorAt((VALUE - minValue) / getSkinnable().getRange())); }
        if (minValue > 0) {
            bar.setLength(((VALUE - minValue) * (-1)) * angleStep);
        } else {
            if (VALUE < 0) {
                bar.setLength((-VALUE + minValue) * angleStep);
            } else {
                bar.setLength(((minValue - VALUE) * angleStep));
            }
        }
        valueText.setText(String.format(Locale.US, "%." + getSkinnable().getDecimals() + "f", VALUE));
        resizeValueText();
    }


    // ******************** Resizing ******************************************
    private void redraw() {
        titleText.setText(getSkinnable().getTitle());
        resizeTitleText();

        unitText.setText(getSkinnable().getUnit());
        resizeUnitText();

        bar.setStroke(getSkinnable().getBarColor());
        colorRing.setStroke(getSkinnable().getBarColor());
        background.setFill(getSkinnable().getBackgroundPaint());
        valueText.setFill(getSkinnable().getValueColor());
        unitText.setFill(getSkinnable().getUnitColor());
        titleText.setFill(getSkinnable().getTitleColor());
        separator.setStroke(getSkinnable().getBorderPaint());
    }

    private void resizeTitleText() {
        double maxWidth = 0.56667 * size;
        titleText.setFont(Fonts.robotoLight(size * 0.08));
        if (titleText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(titleText, maxWidth, 0.13, size); }
        titleText.relocate((size - titleText.getLayoutBounds().getWidth()) * 0.5, size * 0.225);
    }
    private void resizeValueText() {
        double maxWidth = 0.5 * size;
        valueText.setFont(Fonts.robotoRegular(size * 0.3));
        if (valueText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(valueText, maxWidth, 0.35, size); }
        valueText.relocate((size - valueText.getLayoutBounds().getWidth()) * 0.5, (size - valueText.getLayoutBounds().getHeight()) * 0.5);
    }
    private void resizeUnitText() {
        double maxWidth = 0.56667 * size;
        unitText.setFont(Fonts.robotoLight(size * 0.08));
        if (unitText.getLayoutBounds().getWidth() > maxWidth) { Helper.adjustTextSize(unitText, maxWidth, 0.13, size); }
        unitText.relocate((size - unitText.getLayoutBounds().getWidth()) * 0.5, size * 0.66);
    }

    private void resize() {
        double width  = getSkinnable().getWidth() - getSkinnable().getInsets().getLeft() - getSkinnable().getInsets().getRight();
        double height = getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom();
        size          = width < height ? width : height;

        if (width > 0 && height > 0) {
            pane.setMaxSize(size, size);
            pane.relocate((width - size) * 0.5, (height - size) * 0.5);

            colorRing.setCenterX(size * 0.5);
            colorRing.setCenterY(size * 0.5);
            colorRing.setRadius(size * 0.5);
            colorRing.setStrokeWidth(size * 0.0075);
            colorRing.setStrokeType(StrokeType.INSIDE);

            bar.setCenterX(size * 0.5);
            bar.setCenterY(size * 0.5);
            bar.setRadiusX(size * 0.4135);
            bar.setRadiusY(size * 0.4135);
            bar.setStrokeWidth(size * 0.12);

            separator.setStartX(size * 0.5);
            separator.setStartY(size * 0.0275);
            separator.setEndX(size * 0.5);
            separator.setEndY(size * 0.138);

            background.setCenterX(size * 0.5);
            background.setCenterY(size * 0.5);
            background.setRadius(size * 0.363);

            resizeTitleText();
            resizeValueText();
            resizeUnitText();
        }
    }
}
