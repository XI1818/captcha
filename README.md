# captcha
🚀 通过Java和SpringBoot，创建了一个功能完整的验证码系统，包括随机字符串生成、图像绘制、前端展示以及后端验证，为应用增加了一层安全保护！🛡️


![](https://files.mdnice.com/user/57986/66b94aa1-67b8-497b-8d5e-bf3c6fd95beb.png)

# Java 验证码开发（一）

![1-新建项目](https://files.mdnice.com/user/57986/150214c0-7b08-4483-8989-f628146e74a0.png)

---

![2-创建springboot项目](https://files.mdnice.com/user/57986/c4d16d42-a12e-4d00-bb32-5ee9e65a8f87.png)

![3-选择需要注入的依赖](https://files.mdnice.com/user/57986/c0161450-64c6-4341-bd82-c11fa6c4a9b3.png)

上述是`SpringBoot` 项目的搭建工作，搭建完成之后，我们开始本次项目的基础部分介绍。

## 前言

> **今天来介绍一下普通型验证码。**</br> 1.首先生成一随机字符串。字符串的内容可以根据需求设置为英文小写、英文大写、数字、特殊字符、中文。特殊字符和中文不便输入，实际应用中只需英文小写和数字，这里还需除去一些容易容易识别的字符，如：‘1’、‘0’、‘o’、‘l’等。</br> 2.将生成的字符串转为图片。</br>
> （a）将每个字符都生成一张透明背景正方形的图片；</br>
> （b）将每张小图片合并在底图上，小图片随机旋转、缩放、扭曲，清除字符之间的距离；</br>
> （c）在生成的大图上设置 阴影、波纹等效果，加上干扰线、噪点等，扭曲图片，加边框。</br>

> 3.将验证码字符串存入数据库或者缓存中，设置有效时间；将图片 base64 字符串和随机字符串 token 传给前端。</br>

> 4.用户根据图片上的内容，输入验证码；提交时将验证码和 token 传入后端；后端根据 token 取出数据库中验证码，忽略大小比较与用户输入的验证码。

## 一、生成验证码字符串

```java
import java.util.Random;

/**
 * @description 验证码文本生成类
 * @date 2020/8/20
 **/
public class TextCreatorUtils {
    /**
     * 生成验证码所需字符，除去容易识别的字符
     */
    private static char[] charSequence = {
            'A', 'B', 'D', 'E', 'F', 'G',
            'H', 'J', 'K', 'P', 'Q', 'R',
            'T', 'Y', 'Z', '2', '3', '4',
            '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f', 'y',
            'n', 'm', 'n', 'p', 'w', 'x'
    };
    /**
     * 根据系统时间创建随机数
     */
    private static Random random = new Random(System.currentTimeMillis());
    /**
     * 验证码长度
     */
    private static final int LENGTH = 5;

    private TextCreatorUtils(){

    }

    /**
     * 产生验证码文本
     * @return 验证码字符串
     */
    public static String getText() {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < LENGTH; ++i) {
            text.append(charSequence[random.nextInt(charSequence.length)]);
        }
        return text.toString();
    }
}

```

## 二、字符串绘制图片

> 将字符串绘制成图片，每个字符自由旋转、缩放、扭曲，消除字符与字符间的间隙。

```Java
/**
 * @description 文本渲染器，将验证码文字转为图片，能够使文字粘连、扭曲、旋转、缩放
 **/
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
```

## 三、产生验证码图片

> 给图片设置阴影、波纹等效果，加上干扰线、噪点等，扭曲图片，加边框。

```java
/**
 * @description 图片产生类
 **/
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
```

## 四、SpringBoot 创建 captcha demo

### 1.依赖 `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.2.5</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.example</groupId>
	<artifactId>captcha</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>captcha</name>
	<description>captcha</description>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<!--插入依赖-->

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-redis</artifactId>
		</dependency>

		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-lang3</artifactId>
			<version>3.7</version>
		</dependency>

		<dependency>
			<groupId>com.jhlabs</groupId>
			<artifactId>filters</artifactId>
			<version>2.0.235-1</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-thymeleaf</artifactId>
		</dependency>

		<dependency>
			<groupId>com.jhlabs</groupId>
			<artifactId>filters</artifactId>
			<version>2.0.235-1</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<optional>true</optional>
		</dependency>

	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.project-lombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>

</project>

```

### 2.配置 redis

```yml
spring:
  data:
    redis:
      host: 127.0.0.1
      database: 0
      #数据库，默认为0
      password:
      #如果有密码
      timeout: 10000ms
      #客户端超时时间单位是毫秒 默认是2000
      port: 6379
      #redis端口号
server:
  port: 9096

```

### 3.model

> 在 com.cxz.captcha 下建立 model 包，新建 CaptchaVO 和 ResponseModel 两个实体类

```Java
/**
 * @description CaptchaVO
 **/
public class CaptchaVO implements Serializable {

    /**
     * 验证码base64图片
     */
    private String base64Image;

    /**
     * 认证
     */
    private String token;

    /**
     * 验证码文本
     */
    private String captchaCode;

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }
}
```

</br>

```java

/**
 * @description ResponseModel
 **/
public class ResponseModel implements Serializable {
    /**
     * 定义程序序列化ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    private String repCode;

    /**
     * 响应信息
     */
    private String repMsg;

    /**
     * 响应数据
     */
    private Object repData;

    //构造方法
    public ResponseModel() {
        this.repCode = RepCodeEnum.SUCCESS.getCode();
    }

    public ResponseModel(RepCodeEnum repCodeEnum) {
        this.setRepCodeEnum(repCodeEnum);
    }

    //成功
    public static ResponseModel success() {
        return ResponseModel.successMsg("成功");
    }

    public static ResponseModel successMsg(String message) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setRepMsg(message);
        return responseModel;
    }

    public static ResponseModel successData(Object data) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setRepCode(RepCodeEnum.SUCCESS.getCode());
        responseModel.setRepData(data);
        return responseModel;
    }

    //失败
    public static ResponseModel errorMsg(RepCodeEnum message) {
        ResponseModel responseModel = new ResponseModel();
        responseModel.setRepCodeEnum(message);
        return responseModel;
    }

    public static ResponseModel errorMsg(String message){
        ResponseModel responseModel=new ResponseModel();
        responseModel.setRepCode(RepCodeEnum.ERROR.getCode());
        responseModel.setRepMsg(message);
        return responseModel;
    }

    public static ResponseModel errorMsg(RepCodeEnum repCodeEnum,String message){
        ResponseModel responseModel=new ResponseModel();
        responseModel.setRepCode(repCodeEnum.getCode());
        responseModel.setRepMsg(message);
        return responseModel;
    }

    public static ResponseModel exceptionMsg(String message){
        ResponseModel responseModel=new ResponseModel();
        responseModel.setRepCode(RepCodeEnum.EXCEPTION.getCode());
        responseModel.setRepMsg(RepCodeEnum.EXCEPTION.getDesc()+"："+message);
        return responseModel;
    }

    public void setRepCodeEnum(RepCodeEnum repCodeEnum) {
        this.repCode = repCodeEnum.getCode();
        this.repMsg = repCodeEnum.getDesc();
    }

    public String getRepCode() {
        return repCode;
    }

    public void setRepCode(String repCode) {
        this.repCode = repCode;
    }

    public String getRepMsg() {
        return repMsg;
    }

    public void setRepMsg(String repMsg) {
        this.repMsg = repMsg;
    }

    public Object getRepData() {
        return repData;
    }

    public void setRepData(Object repData) {
        this.repData = repData;
    }
}
```

### 4.enums

> 建立 enums 包，新建 RepCodeEnum

```Java
public enum RepCodeEnum {
    SUCCESS("0000","成功"),
    ERROR("0001","操作失败"),
    EXCEPTION("9999","服务器内部异常"),

    NULL_ERROR("0011","{0}不能为空"),

    API_CAPTCHA_INVALID("6110","验证码已失效，请重新获取"),
    API_CAPTCHA_COORDINATE_ERROR("6111","验证码失败"),
    API_CAPTCHA_ERROR("6112","获取验证码失败");

    private String code;
    private String desc;

    RepCodeEnum(String code, String desc){
        this.code=code;
        this.desc=desc;
    }

    public String getCode(){
        return code;
    }

    public String getDesc(){
        return desc;
    }

    public ResponseModel parseError(Object... fieldNames){
        ResponseModel errorMessage=new ResponseModel();
        String newDesc= MessageFormat.format(this.desc,fieldNames);

        errorMessage.setRepCode(this.code);
        errorMessage.setRepMsg(newDesc);
        return errorMessage;
    }

}
```

### 5.service

> 建立`service`包，新建`CaptchaService`接口

```Java
public interface CaptchaService {

    /**
     * 创建验证码
     * @return 创建信息
     */
    ResponseModel createCaptcha();

    /**
     * 验证用户输入的验证码
     * @param captchaVO 认证token,用户输入的验证码captchaWord
     * @return 验证结果
     */
    ResponseModel verifyCaptcha(CaptchaVO captchaVO);
}
```

### 6.util

> 建立 util 包，将本文一、二、三中的类导入

### 7.control

> 建立`control`包，新建`CaptchaController`类

```java
/**
 * @description CaptchaController
 **/
@RestController
public class CaptchaController {
    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/createcaptcha")
    public ResponseModel createCaptcha() {
        return captchaService.createCaptcha();
    }

    @PostMapping("/verify")
    public ResponseModel verify(CaptchaVO captchaVO) {
        return captchaService.verifyCaptcha(captchaVO);
    }
}
```

### 8.简单的前端页面

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>验证码测试</title>
    <link rel="shortcut icon" href="favicon.ico" type="image/x-icon">

    <script src="js/jquery-1.11.1.min.js" type="text/javascript"></script>
</head>
<body>
<div>
    <form action="/verify" method="post">
        <h4>验证码</h4>
        <img src="" id="tp">
        <input type="text" placeholder="请输入验证码" autocomplete="off" name="captchaCode">
        <input type="hidden" id="token" name="token">
        <input type="submit" value="提交">
    </form>
</div>

<script>
    $(function () {
        function captcha() {
            $.ajax({
                type: 'get',
                dataType: 'json',
                url: '/createcaptcha',
                xhrFields: {
                    withCredentials: true
                },
                success: function (data) {
                    if (data.success) {
                        var img = 'data:image/jpg;base64,' + data.repData.base64Image;
                        var token = data.repData.token;
                        $("#tp").attr("src", img);
                        $("#token").attr("value", token);
                    } else {
                        alert("error");
                    }
                }
            });
        };
        captcha();
        $("#tp").click(captcha);
    })

</script>

</body>
</html>

```

**测试接口**

```url
GET
http://localhost:9096/createcaptcha
```

```URL
POST
http://localhost:9096/verify
```

![验证码接口校验成功](https://files.mdnice.com/user/57986/0efb69d3-e9c7-413f-a958-a4688831e00d.png)

![第二个控制接口运行成功](https://files.mdnice.com/user/57986/4732f17a-d781-433c-ae3c-16eb1650214b.png)

---

❓ **解决静态资源加载不出来的问题**

![一个呆滞的错误BUG](https://files.mdnice.com/user/57986/bb618bd1-c6a7-4990-b0df-ddecf9dc08dc.png)

```url
http://localhost:9096/static/index.html
路径错误：静态资源无需添加static,直接访问
```
**http://localhost:9096/index.html**



::: block-1
### 错误 1 示例
```js
          if (data.success) {
            var img = 'data:image/jpg;base64,' + data.repData.base64Image;
            var token = data.repData.token;
            $("#tp").attr("src", img);
            $("#token").attr("value", token);
          } else {
            alert("error");
          }
```
> 返回值错误
![代码写错了😭](https://files.mdnice.com/user/57986/39e2b465-0073-4cec-a36c-bdb6f5fb518f.png)

:::

::: block-2
### 正确 2 示例
```js
          if (data.reCode==='0000') {
            var img = 'data:image/jpg;base64,' + data.repData.base64Image;
            var token = data.repData.token;
            $("#tp").attr("src", img);
            $("#token").attr("value", token);
          } else {
            alert("error");
          }
```
> `debugger` 调试之后正确写法



          
:::

### 9.效果

> Jquery下载链接合集

**https://blog.csdn.net/xsq123/article/details/125291010**

<![验证码1](https://files.mdnice.com/user/57986/b9c62955-abca-4466-8486-4cf10427858f.png),![验证码2](https://files.mdnice.com/user/57986/2405ad8e-1b40-4920-ae1a-5aa2c0806e53.png),![验证码3](https://files.mdnice.com/user/57986/6c36ba3d-ddad-470c-8770-59977be24d03.png)>

**项目基础目录**
```java
C:\USERS\27107\DESKTOP\CODE\JAVASTUDY\COMPONENTS\CAPTCHA\SRC
├─main
│  ├─java
│  │  └─com
│  │      └─example
│  │          └─captcha
│  │              │  CaptchaApplication.java
│  │              │
│  │              ├─controller
│  │              │      CaptchaController.java
│  │              │
│  │              ├─enums
│  │              │      RepCodeEnum.java
│  │              │
│  │
│  └─resources
│      │  application.yml
│      │
│      ├─static
│      │  │  index.html
│      │  │
│      │  └─js
│      │          jquery-1.11.1.min.js
│      │
│      └─templates
└─test
    └─java
        └─com
            └─example
                └─captcha
                        CaptchaApplicationTests.java


```

![](https://files.mdnice.com/user/57986/ad7167a4-3422-4220-9807-3cb2f2802ab3.png)



![](https://files.mdnice.com/user/57986/132c353b-a54e-4ca9-b16a-470c294b88f5.png)

