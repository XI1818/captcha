package com.example.captcha.util;

import com.jhlabs.image.RippleFilter;
import com.jhlabs.image.ShadowFilter;
import com.jhlabs.image.TransformFilter;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Random;

/**
 * 功能：
 * 作者：兮和
 * 时间：2024/5/4 17:49
 */
public class ImageCreatorUtils {

    private static int width = 200;
    private static int height = 50;
    /* 边框颜色 */
    private static Color borderColor = Color.gray;
    /* 边框宽度 */
    private static int borderThickness = 1;

    /* 随机数 */
    private static Random random = new Random();

    /* 是否画边框 */
    private static boolean isBorderDrawn = true;

    /* 是否有阴影 */
    private static boolean isShadow = false;

    /* 是否有波纹 */
    private static boolean isRipple = false;

    /* 是否制造干扰 */
    private static boolean isMakeNoise = false;

    /* 图片是否扭曲 */
    private static boolean isShear = false;

    private ImageCreatorUtils() {

    }

    /**
     * 产生验证码图片
     *
     * @param text 验证码文本
     * @return 验证码图片
     */
    public static BufferedImage createImage(String text) {
        BufferedImage bi = WordRendererUtils.renderWord(text, width, height);
        bi = getDistortedImage(bi);
        bi = addBackground(bi);
        Graphics2D graphics = bi.createGraphics();
        if (isBorderDrawn) {
            drawBox(graphics);
        }

        return bi;
    }

    /**
     * 添加背景
     *
     * @param baseImage 基图
     * @return 加了背景的图片
     */
    private static BufferedImage addBackground(BufferedImage baseImage) {
        Color colorFrom = Color.WHITE;
        Color colorTo = Color.WHITE;
        int width = baseImage.getWidth();
        int height = baseImage.getHeight();
        BufferedImage imageWithBackground = new BufferedImage(width, height, 1);
        Graphics2D graph = (Graphics2D) imageWithBackground.getGraphics();
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        hints.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));
        hints.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        graph.setRenderingHints(hints);
        GradientPaint paint = new GradientPaint(0.0F, 0.0F, colorFrom, (float) width, (float) height, colorTo);
        graph.setPaint(paint);
        graph.fill(new Rectangle2D.Double(0.0D, 0.0D, (double) width, (double) height));
        graph.drawImage(baseImage, 0, 0, (ImageObserver) null);
        return imageWithBackground;
    }

    /**
     * 添加边框
     *
     * @param graphics 画笔
     */
    private static void drawBox(Graphics2D graphics) {
        graphics.setColor(borderColor);
        if (borderThickness != 1) {
            BasicStroke stroke = new BasicStroke((float) borderThickness);
            graphics.setStroke(stroke);
        }

        Line2D line1 = new Line2D.Double(0.0D, 0.0D, 0.0D, (double) width);
        graphics.draw(line1);
        Line2D line2 = new Line2D.Double(0.0D, 0.0D, (double) width, 0.0D);
        graphics.draw(line2);
        line2 = new Line2D.Double(0.0D, (double) (height - 1), (double) width, (double) (height - 1));
        graphics.draw(line2);
        line2 = new Line2D.Double((double) (width - 1), (double) (height - 1), (double) (width - 1), 0.0D);
        graphics.draw(line2);
    }

    /**
     * 制作干扰
     *
     * @param image
     * @param factorOne
     * @param factorTwo
     * @param factorThree
     * @param factorFour
     */
    private static void makeNoise(BufferedImage image, float factorOne, float factorTwo, float factorThree, float factorFour) {
        Color color = Color.BLUE;
        int width = image.getWidth();
        int height = image.getHeight();
        Point2D[] pts = null;
        CubicCurve2D cc = new CubicCurve2D.Float((float) width * factorOne, (float) height * random.nextFloat(), (float) width * factorTwo, (float) height * random.nextFloat(), (float) width * factorThree, (float) height * random.nextFloat(), (float) width * factorFour, (float) height * random.nextFloat());
        PathIterator pi = cc.getPathIterator((AffineTransform) null, 2.0D);
        Point2D[] tmp = new Point2D[200];
        int i = 0;

        while (!pi.isDone()) {
            float[] coords = new float[6];
            switch (pi.currentSegment(coords)) {
                case 0:
                case 1:
                    tmp[i] = new Point2D.Float(coords[0], coords[1]);
                default:
                    ++i;
                    pi.next();
            }
        }

        pts = new Point2D[i];
        System.arraycopy(tmp, 0, pts, 0, i);
        Graphics2D graph = (Graphics2D) image.getGraphics();
        graph.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        graph.setColor(color);

        for (i = 0; i < pts.length - 1; ++i) {
            if (i < 3) {
                graph.setStroke(new BasicStroke(0.9F * (float) (4 - i)));
            }
            graph.drawLine((int) pts[i].getX(), (int) pts[i].getY(), (int) pts[i + 1].getX(), (int) pts[i + 1].getY());
        }
        graph.dispose();
    }

    /**
     * 图片样式
     *
     * @param baseImage 基图
     * @return 改变样式后的图片
     */
    private static BufferedImage getDistortedImage(BufferedImage baseImage) {
        BufferedImage distortedImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graph = (Graphics2D) distortedImage.getGraphics();
        if (isShadow) {
            ShadowFilter shadowFilter = new ShadowFilter();
            shadowFilter.setRadius(10);
            shadowFilter.setDistance(3);
            shadowFilter.setOpacity(1);
            baseImage = shadowFilter.filter(baseImage, null);
        }

        if (isRipple) {
            RippleFilter rippleFilter = new RippleFilter();
            rippleFilter.setWaveType(RippleFilter.SINE);
            rippleFilter.setXAmplitude(7.5f);
            rippleFilter.setYAmplitude(0.5f);
            rippleFilter.setXWavelength(random.nextInt(7) + 8);
            rippleFilter.setYWavelength(random.nextInt(3) + 2);
            rippleFilter.setEdgeAction(TransformFilter.BILINEAR);
            baseImage = rippleFilter.filter(baseImage, null);
        }

        graph.drawImage(baseImage, 0, 0, null, null);

        if (isShear) {
            shear(graph, baseImage.getWidth(), baseImage.getHeight());
        }

        graph.dispose();

        if (isMakeNoise) {
            makeNoise(distortedImage, .1f, .3f, .6f, .9f);
        }

        return distortedImage;
    }

    /**
     * 字符和干扰线扭曲
     *
     * @param g  绘制图形的java工具类
     * @param w1 验证码图片宽
     * @param h1 验证码图片高
     */
    private static void shear(Graphics g, int w1, int h1) {
        shearX(g, w1, h1);
        shearY(g, w1, h1);
    }


    /**
     * x轴扭曲
     *
     * @param g  绘制图形的java工具类
     * @param w1 验证码图片宽
     * @param h1 验证码图片高
     */
    private static void shearX(Graphics g, int w1, int h1) {
        int period = random.nextInt(2);

        boolean borderGap = true;
        int frames = 1;
        int phase = random.nextInt(2);

        for (int i = 0; i < h1; i++) {
            double d = (double) (period >> 1) * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(0, i, w1, 1, (int) d, 0);
            if (borderGap) {
                g.drawLine((int) d, i, 0, i);
                g.drawLine((int) d + w1, i, w1, i);
            }
        }
    }

    /**
     * y轴扭曲
     *
     * @param g  绘制图形的java工具类
     * @param w1 验证码图片宽
     * @param h1 验证码图片高
     */
    private static void shearY(Graphics g, int w1, int h1) {
        int period = random.nextInt(10) + 5;

        boolean borderGap = true;
        int frames = 20;
        int phase = 7;
        for (int i = 0; i < w1; i++) {
            double d = (double) (period >> 1) * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(i, 0, 1, h1, 0, (int) d);
            if (borderGap) {
                g.drawLine(i, (int) d, i, 0);
                g.drawLine(i, (int) d + h1, i, h1);
            }
        }
    }
}

