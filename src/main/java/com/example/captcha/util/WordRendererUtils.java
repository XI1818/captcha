package com.example.captcha.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * 功能：文本渲染器，将验证码文字转为图片，能够使文字粘连、扭曲、旋转、缩放
 * 作者：兮和
 * 时间：2024/5/4 17:50
 */
public class WordRendererUtils {
    /* 对验证码各个字符进行变换时的缩放因子范围，包括x方向和y方向 */
    private static double[] scaleRange = new double[]{0.5, 1};
    /* 验证码各个字符进行变换时的旋转因子范围，单位为弧度 */
    private static double[] rotateRange = new double[]{-Math.PI / 5, Math.PI / 5};
    /* 验证码各个字符进行变换时的切变因子范围，包括x方向和y方向 */
    private static double[] shearRange = new double[]{0, 0};
    /* 字体 */
    private static Font font = new Font("TimesRoman", Font.BOLD, 40);
    /* 颜色 */
    private static Color color = Color.BLUE;

    private WordRendererUtils() {
    }

    /**
     * 将验证码文本转为图片
     *
     * @param word   验证码文本
     * @param width  图片宽度
     * @param height 图片高度
     * @return 验证码文字图像
     */
    public static BufferedImage renderWord(String word, int width, int height) {
        //将验证码字符串转为数组
        char[] codeChars = word.toCharArray();
        //每个字符生成一个图片
        BufferedImage[] images = new BufferedImage[codeChars.length];
        //将word中每个char转为图片
        for (int i = 0; i < codeChars.length; i++) {
            //创建一个height边长的正方形图片
            images[i] = new BufferedImage(height, height, 2);
            //获取画笔
            Graphics2D graphics2D = images[i].createGraphics();
            //设置字体
            graphics2D.setFont(font);
            //设置
            graphics2D.setColor(color);
            //设置抗锯齿
            RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
            graphics2D.setRenderingHints(hints);
            //设置缩放（scale）、旋转（rotate）、错切（shear）
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.scale(getRandomInRange(scaleRange), getRandomInRange(scaleRange));
            affineTransform.rotate(getRandomInRange(rotateRange), height / 2.0, height / 2.0);
            affineTransform.shear(getRandomInRange(shearRange), 0);
            graphics2D.setTransform(affineTransform);
            //文字在图片中的位置，x轴水平居中
            int x = (height - graphics2D.getFontMetrics().stringWidth(Character.toString(codeChars[i]))) / 2;
            //坐标轴是从左上角开始的，y轴向下，所以这里用height-x
            int y = height - x;
            //画出这个文字
            graphics2D.drawString(Character.toString(codeChars[i]), x, y);
            //释放
            graphics2D.dispose();
        }

        //返回每个字符图像合并图像
        return appendImages(images, width, height);
    }

    /**
     * 拼接每个字符图片
     *
     * @param images 文字图片数组
     * @param width  图片宽度
     * @param height 图片高度
     * @return 合并后的图片
     */
    private static BufferedImage appendImages(BufferedImage[] images, int width, int height) {
        //创建一个width*height大小的黑色背景图片
        BufferedImage bgImage = new BufferedImage(width, height, 2);
        //获得画笔
        Graphics2D graphics2D = bgImage.createGraphics();
        //当前正在处理的图片序号
        int index = 0;
        //计算字符与字符间的总空隙
        int d = 0;
        for (int i = 1; i < images.length; i++) {
            int distance = calculateDistanceBetweenChar2(images[i - 1], images[i]);
            d += distance;
        }
        //使验证码水平居中
        int drawX = 0;
        if (images.length * images[0].getWidth() - d < width) {
            drawX = (width - (images.length * images[0].getWidth() - d)) / 2;
        }
        //将第一个文字图片画到底图上
        graphics2D.drawImage(images[index], drawX, 0, images[index].getWidth(), images[0].getHeight(), null);
        drawX += images[index].getWidth();
        index++;
        //将1-images.length个图片画到底图上。并取消文字间隙，使文字粘连在一起
        while (index < images.length) {
            //加上2是为了使粘连更紧凑
            int distance = calculateDistanceBetweenChar2(images[index - 1], images[index]) + 2;
            graphics2D.drawImage(images[index], drawX - distance, 0, images[index].getWidth(), images[0].getHeight(), null);
            drawX += images[index].getWidth() - distance;
            index++;
        }
        graphics2D.dispose();
        return bgImage;
    }

    /**
     * 按行扫描
     *
     * @param leftImage  左边图像
     * @param rightImage 右边图像
     * @return 两图片之间的空隙
     */
    private static int calculateDistanceBetweenChar2(BufferedImage leftImage, BufferedImage rightImage) {
        //左图每行右侧空白字符个数列表
        int[][] left = calculateBlankNum(leftImage);
        //右图每行左侧空白字符个数列表
        int[][] right = calculateBlankNum(rightImage);

        int[] tempArray = new int[leftImage.getHeight()];
        for (int i = 0; i < left.length; i++) {
            if (right[i][0] == 0) {
                tempArray[i] = left[i][1] + leftImage.getWidth();
            } else {
                tempArray[i] = left[i][1] + right[i][0];
            }
        }
        return min(tempArray);
    }

    /**
     * 计算每个图片每行的左右空白像素个数，int[row][0]存储左边空白像素个数int[row][1]存储右边空白像素个数
     *
     * @param image 图像
     * @return 空白字符个数数组
     */
    private static int[][] calculateBlankNum(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[][] result = new int[height][2];
        for (int i = 0; i < result.length; i++) {
            result[i][0] = 0;
            result[i][1] = width;
        }

        int[] colorArray = new int[4];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                colorArray = image.getRaster().getPixel(j, i, colorArray);
                if (!checkArray(colorArray, new int[]{0, 0, 0, 0})) {
                    if (result[i][0] == 0) {
                        result[i][0] = j;
                    }
                    result[i][1] = width - j - 1;
                }
            }
        }
        return result;
    }

    /**
     * 检查两数组是否相同
     *
     * @param arrayA A数组
     * @param arrayB B数组
     * @return 是否相同
     */
    private static boolean checkArray(int[] arrayA, int[] arrayB) {
        if (arrayA == null || arrayB == null) {
            return false;
        }
        if (arrayA.length != arrayB.length) {
            return false;
        }
        for (int i = 0; i < arrayA.length; i++) {
            if (arrayA[i] != arrayB[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 找出数组中最小元素
     *
     * @param array 数组
     * @return 数组中最小值
     */
    private static int min(int[] array) {
        int result = Integer.MAX_VALUE;
        for (int item : array) {
            if (item < result) {
                result = item;
            }
        }
        return result;
    }

    /**
     * 获取一个指定范围内的double随机数
     *
     * @param range 范围
     * @return 随机数
     */
    private static double getRandomInRange(double[] range) {
        if (range == null || range.length != 2) {
            throw new RuntimeException("至少包含两个元素");
        }
        return Math.random() * (range[1] - range[0]) + range[0];
    }
}
