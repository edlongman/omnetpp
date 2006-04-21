package org.omnetpp.scave.editors;

import java.awt.Font;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.AbstractBlock;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;

import org.omnetpp.scave.charting.ChartSWTWrapper;
import org.omnetpp.scave.properties.EnumPropertyDescriptor;
import org.omnetpp.scave.properties.Property;
import org.omnetpp.scave.properties.PropertySource;

public class ChartProperties extends PropertySource {

	private ChartSWTWrapper swtChart;
	
	public ChartProperties(ChartSWTWrapper swtChart)
	{
		this.swtChart = swtChart;
	}
	
	public JFreeChart getChart() {
		return swtChart.getChart();
	}
	
	@Property(displayName = "Title")
	public TitleProperties getTitle() {
		return new TitleProperties(getChart().getTitle());
	}
	
	@Property(displayName = "Legend")
	public LegendProperties getLegend() {
		return new LegendProperties(getChart().getLegend());
	}
	
	@Property(displayName = "Plot")
	public PlotProperties getPlot() {
		Plot plot = getChart().getPlot();
		if (plot instanceof CategoryPlot)
			return new CategoryPlotProperties((CategoryPlot)plot);
		else if (plot instanceof XYPlot)
			return new XYPlotProperties((XYPlot)plot);
		else
			return null;
	}
	
	@Property(displayName = "Axes")
	public AxesProperties getAxes() {
		Plot plot = getChart().getPlot();
		if (plot instanceof CategoryPlot)
			return new CategoryPlotAxesProperties((CategoryPlot)plot);
		else if (plot instanceof XYPlot)
			return new XYPlotAxesProperties((XYPlot)plot);
		else
			return null;
	}
	
	private void refresh() {
		swtChart.refresh();
	}
	

	public class TitleProperties extends BlockProperties
	{
		private TextTitle title;
		
		public TitleProperties(TextTitle title)
		{
			super(title);
			this.title = title;
		}
		
		@Property
		public String getText() { return title.getText(); }
		public void setText(String text) { title.setText(text); }
		
		@Property
		public FontData getFont() { return fontToFontData(title.getFont()); }
		public void setFont(FontData font) { title.setFont(fontDataToFont(font)); }

		@Property
		public RGB getTextColor() { return paintToRGB(title.getPaint()); }
		public void setTextColor(RGB rgb) { title.setPaint(rgbToPaint(rgb)); }

		@Property(descriptorClass = EnumPropertyDescriptor.class,
				  category = "Layout")
		public RectangleEdge getPosition() { return title.getPosition(); }
		public void setPosition(RectangleEdge pos) { title.setPosition(pos); }
		
		@Property(descriptorClass = EnumPropertyDescriptor.class,
				  category = "Layout")
		public HorizontalAlignment getHorizontalAlignment() { return title.getHorizontalAlignment(); }
		public void setHorizontalAlignment(HorizontalAlignment alignment) { title.setHorizontalAlignment(alignment); }
		public HorizontalAlignment defaultHorizontalAlignment() { return Title.DEFAULT_HORIZONTAL_ALIGNMENT; }
		
		@Property(descriptorClass = EnumPropertyDescriptor.class,
				  category = "Layout")
		public VerticalAlignment getVerticalAlignment() { return title.getVerticalAlignment(); }
		public void setVerticalAlignment(VerticalAlignment alignment) { title.setVerticalAlignment(alignment); }
		public VerticalAlignment defaultVerticalAlignment() { return Title.DEFAULT_VERTICAL_ALIGNMENT; }
	}
	
	public class LegendProperties extends BlockProperties
	{
		private LegendTitle legend;
		
		public LegendProperties(LegendTitle legend)
		{
			super(legend);
			this.legend = legend;
		}
		
		@Property
		public RGB getBackgroundColor() { return paintToRGB(legend.getBackgroundPaint()); }
		public void setBackgroundColor(RGB rgb) { legend.setBackgroundPaint(rgbToPaint(rgb)); refresh(); }
		
		@Property(descriptorClass = EnumPropertyDescriptor.class)
		public RectangleEdge getPosition() { return legend.getPosition(); }
		public void setPosition(RectangleEdge pos) { legend.setPosition(pos); refresh(); }
		
		@Property
		public FontData getItemFont() { return fontToFontData(legend.getItemFont()); }
		public void setItemFont(FontData fd) { legend.setItemFont(fontDataToFont(fd)); refresh(); }

		@Property
		public RGB getItemColor() { return paintToRGB(legend.getItemPaint()); }
		public void setItemColor(RGB rgb) { legend.setItemPaint(rgbToPaint(rgb)); refresh(); }
	}
	
	public class PlotProperties extends PropertySource
	{
		Plot plot;
		
		public PlotProperties(Plot plot) {
			this.plot = plot;
		}
		
		@Property(category = "Background")
		public RGB getBackgroundColor() { return paintToRGB(plot.getBackgroundPaint()); }
		public void setBackgroundColor(RGB rgb) { plot.setBackgroundPaint(rgbToPaint(rgb)); }
		public RGB defaultBackgroundColor() { return paintToRGB(Plot.DEFAULT_BACKGROUND_PAINT); }
		
		@Property(category = "Background")
		public double getBackgroundAlpha() { return plot.getBackgroundAlpha(); }
		public void setBackgroundAlpha(double alpha) { plot.setBackgroundAlpha((float)alpha); }
		public double defaultBackgroundAlpha() { return Plot.DEFAULT_BACKGROUND_ALPHA; }
		
		@Property(category = "Background")
		public Image getBackgroundImage() { return plot.getBackgroundImage(); }
		public void setBackgroundImage(Image image) { plot.setBackgroundImage(image); }
		public Image defaultBackgroundImage() { return null; }
		
		@Property(category = "Background")
		public int getBackgroundImageAlignment() { return plot.getBackgroundImageAlignment(); }
		public void setBackgroundImageAlignment(int align) { plot.setBackgroundImageAlignment(align); }
		
		@Property(category = "Outline")
		public RGB getOutlineColor() { return paintToRGB(plot.getOutlinePaint()); }
		public void setOutlineColor(RGB rgb) { plot.setOutlinePaint(rgbToPaint(rgb)); }
		public RGB defaultOutlineColor() { return paintToRGB(Plot.DEFAULT_OUTLINE_PAINT); }
		
		@Property(category = "Outline")
		public InsetsProperties getInsets() { return new InsetsProperties(plot.getInsets()); }
		public void setInsets(InsetsProperties insets) { plot.setInsets(insets.getValue()); }
		public InsetsProperties defaultInsets() { return new InsetsProperties(Plot.DEFAULT_INSETS); }
		
		@Property(category = "Outline")
		public Stroke getOutlineStroke() { return plot.getOutlineStroke(); }
		public void setOutlineStroke(Stroke stroke) { plot.setOutlineStroke(stroke); }
		public Stroke defaultOutlineStroke() { return Plot.DEFAULT_OUTLINE_STROKE; }
		
		@Property(category = "Foreground")
		public double getForegroundAlpha() { return plot.getForegroundAlpha(); }
		public void setForegroundAlpha(double alpha) { plot.setForegroundAlpha((float)alpha); }
		public double defaultForegroundAlpha() { return Plot.DEFAULT_FOREGROUND_ALPHA; }
	}
	
	public static class CategoryRendererKind extends PropertySource
	{
		CategoryItemRenderer renderer;
		static Map<Class,CategoryRendererKind> renderers = new HashMap<Class,CategoryRendererKind>(2);
		
		private CategoryRendererKind(CategoryItemRenderer renderer) {
			this.renderer = renderer;
			renderers.put(renderer.getClass(), this);
		}
		
		public final static CategoryRendererKind
			BAR = new CategoryRendererKind(new BarRenderer()), 
			BAR3D = new CategoryRendererKind(new BarRenderer3D()),
			STACKED_BAR = new CategoryRendererKind(new StackedBarRenderer()),
			STACKED_BAR3D = new CategoryRendererKind(new StackedBarRenderer3D()),
			AREA = new CategoryRendererKind(new AreaRenderer()),
			INTERVAL_BAR = new CategoryRendererKind(new IntervalBarRenderer()),
			LAYERED_BAR = new CategoryRendererKind(new LayeredBarRenderer());
		
		
		public CategoryItemRenderer getRenderer() {
			return renderer;
		}
		
		public static CategoryRendererKind get(CategoryItemRenderer renderer) {
			return renderers.get(renderer.getClass());
		}

		@Override
		public Object getEditableValue() {
			return this;
		}

		double val;
		@Property
		public double getDoubleVal() { return val; }
		public void setDoubleVal(double val) { this.val = val; }
	}

	public class CategoryPlotProperties extends PlotProperties
	{
		CategoryPlot plot;

		public CategoryPlotProperties(CategoryPlot plot) {
			super(plot);
			this.plot = plot;
		}
		
		@Property(descriptorClass = EnumPropertyDescriptor.class)
		public PlotOrientation getOrientation() { return plot.getOrientation(); }
		public void setOrientation(PlotOrientation orientation) { plot.setOrientation(orientation); }
		
		@Property(descriptorClass = EnumPropertyDescriptor.class)
		public CategoryRendererKind getRenderer() { return CategoryRendererKind.get(plot.getRenderer()); }
		public void setRenderer(CategoryRendererKind renderer) { plot.setRenderer(renderer.getRenderer()); }
		
		@Property(category = "Grid")
		public boolean isDomainGridVisible() { return plot.isDomainGridlinesVisible(); }
		public void setDomainGridVisible(boolean value) { plot.setDomainGridlinesVisible(value); }
		public boolean defaultDomainGridVisible() { return CategoryPlot.DEFAULT_DOMAIN_GRIDLINES_VISIBLE; }
		
		@Property(category = "Grid")
		public boolean isRangeGridVisible() { return plot.isRangeGridlinesVisible(); }
		public void setRangeGridVisible(boolean value) { plot.setRangeGridlinesVisible(value); }
		public boolean defaultRangeGridVisible() { return CategoryPlot.DEFAULT_RANGE_GRIDLINES_VISIBLE; }
	}
	
	public static class XYRendererKind
	{
		XYItemRenderer renderer;
		static Map<Class,XYRendererKind> renderers = new HashMap<Class,XYRendererKind>(2);
		
		private XYRendererKind(XYItemRenderer renderer) {
			this.renderer = renderer;
			renderers.put(renderer.getClass(), this);
		}
		
		public final static XYRendererKind
			LINE = new XYRendererKind(new XYLineAndShapeRenderer(true, false)),
			AREA = new XYRendererKind(new XYAreaRenderer()),
			BAR = new XYRendererKind(new XYBarRenderer()),
			DOT = new XYRendererKind(new XYDotRenderer());
		
		public XYItemRenderer getRenderer() {
			return renderer;
		}
		
		public static XYRendererKind get(XYItemRenderer renderer) {
			return renderers.get(renderer.getClass());
		}
	}

	public class XYPlotProperties extends PlotProperties
	{
		XYPlot plot;
		
		public XYPlotProperties(XYPlot plot) {
			super(plot);
			this.plot = plot;
		}
		
		@Property(descriptorClass = EnumPropertyDescriptor.class)
		public XYRendererKind getRenderer() { return XYRendererKind.get(plot.getRenderer()); }
		public void setRenderer(XYRendererKind renderer) { plot.setRenderer(renderer.getRenderer()); }
		
	}
	
	public class AxesProperties extends PropertySource
	{
	}
	
	public class CategoryPlotAxesProperties extends AxesProperties
	{
		CategoryPlot plot;
		
		public CategoryPlotAxesProperties(CategoryPlot plot) {
			this.plot = plot;
		}
		
		@Property
		public AxisProperties getDomainAxis() {
			return new CategoryAxisProperties(plot.getDomainAxis());
		}
		
		@Property
		public AxisProperties getRangeAxis() {
			return new ValueAxisProperties(plot.getRangeAxis());
		}
	}
	
	public class XYPlotAxesProperties extends AxesProperties
	{
		XYPlot plot;
		
		public XYPlotAxesProperties(XYPlot plot) {
			this.plot = plot;
		}

		@Property
		public AxisProperties getDomainAxis() {
			return new ValueAxisProperties(plot.getDomainAxis());
		}
		
		@Property
		public AxisProperties getRangeAxis() {
			return new ValueAxisProperties(plot.getRangeAxis());
		}
	}
	
	public class AxisProperties extends PropertySource
	{
		Axis axis;
		
		public AxisProperties(Axis axis) {
			this.axis = axis;
		}
		
		@Property(category = "Label")
		public String getLabelText() { return axis.getLabel(); }
		public void setLabelText(String label) { axis.setLabel(label); }
		
		@Property(category = "Label")
		public FontData getLabelFont() { return fontToFontData(axis.getLabelFont()); }
		public void setLabelFont(FontData fd) { axis.setLabelFont(fontDataToFont(fd)); }
		
		@Property(category = "Label")
		public RGB getLabelColor() { return paintToRGB(axis.getLabelPaint()); }
		public void setLabelColor(RGB rgb) { axis.setLabelPaint(rgbToPaint(rgb)); }
		
		@Property(category = "TickLabel")
		public boolean isTickLabelsVisible() { return axis.isTickLabelsVisible(); }
		public void setTickLabelsVisible(boolean value) { axis.setTickLabelsVisible(value); }
		
		@Property(category = "TickLabel")
		public FontData getTickLabelFont() { return fontToFontData(axis.getTickLabelFont()); }
		public void setTickLabelFont(FontData fd) { axis.setTickLabelFont(fontDataToFont(fd)); }
		
		@Property(category = "TickLabel")
		public RGB getTickLabelColor() { return paintToRGB(axis.getTickLabelPaint()); }
		public void setTickLabelColor(RGB rgb) { axis.setTickLabelPaint(rgbToPaint(rgb)); }
		
		@Property(category = "TickMark")
		public boolean isTickMarksVisible() { return axis.isTickMarksVisible(); }
		public void setTickMarksVisible(boolean value) { axis.setTickMarksVisible(value); }

		@Property(category = "TickMark")
		public RGB getTickMarksColor() { return paintToRGB(axis.getTickMarkPaint()); }
		public void setTickMarksColor(RGB rgb) { axis.setTickMarkPaint(rgbToPaint(rgb)); }
	}
	

	public class CategoryAxisProperties extends AxisProperties
	{
		CategoryAxis axis;
		
		public CategoryAxisProperties(CategoryAxis axis) {
			super(axis);
			this.axis = axis;
		}

		@Property(descriptorClass = EnumPropertyDescriptor.class)
		public CategoryLabelPositions getLabelPositions() { return axis.getCategoryLabelPositions(); }
		public void setLabelPositions(CategoryLabelPositions pos) { axis.setCategoryLabelPositions(pos); }
	}
	
	public class ValueAxisProperties extends AxisProperties
	{
		ValueAxis axis;
		
		public ValueAxisProperties(ValueAxis axis) {
			super(axis);
			this.axis = axis;
		}
		
		@Property
		public boolean isShowPositiveArrow() { return axis.isPositiveArrowVisible(); }
		public void setShowPositiveArrow(boolean flag) { axis.setPositiveArrowVisible(flag); }
		
		@Property
		public boolean isShowNegativeArrow() { return axis.isNegativeArrowVisible(); }
		public void setShowNegativeArrow(boolean flag) { axis.setNegativeArrowVisible(flag); }
		
		@Property
		public boolean isAutoRange() { return axis.isAutoRange(); }
		public void setAutoRange(boolean flag) { axis.setAutoRange(flag); }
		
		@Property
		public RangeProperties getRange() { return new RangeProperties(axis.getRange()); }
		public void setRange(RangeProperties range) { axis.setRange(range.getValue()); }
		
		public class RangeProperties extends PropertySource
		{
			double min;
			double max;
			
			public RangeProperties(Range range) {
				min = range.getLowerBound();
				max = range.getUpperBound();
			}
			
			public Object getEditableValue() {
				return this;
			}
			
			public Range getValue() {
				return new Range(min, max);
			}
			
			@Property
			public double getMin() { return min; }
			public void setMin(double min) { this.min = min; }
			
			@Property
			public double getMax() { return max; }
			public void setMax(double max) { this.max = max; }
		}
	}
	
	public class BlockProperties extends PropertySource
	{
		AbstractBlock block;
		
		public BlockProperties(AbstractBlock block) {
			this.block = block;
		}
		
		@Property
		public InsetsProperties getMargin() { return new InsetsProperties(block.getMargin()); }
		public void setMargin(InsetsProperties margin) { block.setMargin(margin.getValue()); refresh(); }; 
		
		@Property
		public BorderProperties getBorder() { return new BorderProperties(block.getBorder()); }
		public void setBorder(BorderProperties border) { block.setBorder(border.getValue()); refresh(); };

		@Property
		public InsetsProperties getPadding() { return new InsetsProperties(block.getPadding()); }
		public void setPadding(InsetsProperties padding) { block.setPadding(padding.getValue()); refresh(); };  
	}
	
	public class InsetsProperties extends PropertySource
	{
		public double top;
		public double left;
		public double bottom;
		public double right;
		
		public InsetsProperties(RectangleInsets insets) {
			top = insets.getTop();
			left = insets.getLeft();
			bottom = insets.getBottom();
			right = insets.getRight();
		}
		
		public RectangleInsets getValue() {
			return new RectangleInsets(top, left, bottom, right);
		}
		
		
		@Override
		public Object getEditableValue() {
			return this;
		}
		
		@Property
		public double getTop() { return top; }
		public void setTop(double value) { top = value; }
		
		@Property
		public double getLeft() { return left; }
		public void setLeft(double value) { left = value; }

		@Property
		public double getBottom() { return bottom; }
		public void setBottom(double value) { bottom = value; }

		@Property
		public double getRight() { return right; }
		public void setRight(double value) { right = value; }
	}

	public class BorderProperties extends PropertySource
	{
		RectangleInsets insets;
		Paint paint;
		
		public BorderProperties(BlockBorder border) {
			this.insets = border.getInsets();
			this.paint = border.getPaint();
		}
		
		public Object getEditableValue() {
			return this;
		}
		
		public BlockBorder getValue() {
			return new BlockBorder(insets, paint);
		}
		
		@Property
		public InsetsProperties getInsets() { return new InsetsProperties(insets); }
		public void setInsets(InsetsProperties insets) { this.insets = insets.getValue(); }
		
		@Property
		public RGB getColor() { return paintToRGB(paint); }
		public void setColor(RGB rgb) { paint = rgbToPaint(rgb); } 
	}

	/*
	 * Conversions between AWT and SWT types. 
	 */
	
	private static RGB paintToRGB(Paint paint)
	{
		if (paint instanceof java.awt.Color) {
			java.awt.Color awtColor = (java.awt.Color)paint;
			return new RGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
		}
		return null;
	}
	
	private static Paint rgbToPaint(RGB rgb)
	{
		return new java.awt.Color(rgb.red, rgb.green, rgb.blue);
	}
	
	private static FontData fontToFontData(Font font) {
		return new FontData(font.getName(), font.getSize(), font.getStyle());
	}
	
	private static Font fontDataToFont(FontData fd) {
		return new Font(fd.getName(), fd.getStyle(), fd.getHeight());
	}
}
