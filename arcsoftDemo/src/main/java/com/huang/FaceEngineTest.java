package com.huang;

import cn.hutool.core.util.ImageUtil;
import com.arcsoft.face.*;
import com.arcsoft.face.enums.ImageFormat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceEngineTest {

    public static void main(String[] args) {
        new FaceEngineTest().faceEngineTest();
    }


    public void faceEngineTest() {
        String appId = "GhAV916HtBpZAMeJnD23GbMPwpD8nrwNQDxL6KRHvr4g";
        String sdkKey = "AfCSD9zAFSf4DJ3SNQf8u3u3iMaQ4FDiM7ETG5J2V4hX";

        FaceEngine faceEngine = new FaceEngine();
        //激活引擎
        faceEngine.active(appId, sdkKey);
        EngineConfiguration engineConfiguration = EngineConfiguration.builder().functionConfiguration(
                FunctionConfiguration.builder()
                        .supportAge(true)
                        .supportFace3dAngle(true)
                        .supportFaceDetect(true)
                        .supportFaceRecognition(true)
                        .supportGender(true)
                        .supportLiveness(true)
                        .build()).build();
        //初始化引擎
        faceEngine.init(engineConfiguration);
        File file = new File("C:\\Users\\huangsq\\Desktop\\a\\12.jpg");
        File file2 = new File("C:\\Users\\huangsq\\Desktop\\a\\3.jpg");
        BufferedImage read=null;
        try {
            read = ImageIO.read(file);
        }catch (Exception e){
            e.printStackTrace();
        }
        ImageInfo imageInfo = getRGBData(file);
        ImageInfo imageInfo2 = getRGBData(file2);
        //人脸检测
        List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
        int detectFacesResult1 = faceEngine.detectFaces(imageInfo.getRgbData(), imageInfo.getWidth(), imageInfo.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfoList);
        System.out.println("detectFacesResult1:"+detectFacesResult1);
        FaceInfo faceInfo = faceInfoList.get(0);
        Rect rect = faceInfo.getRect();
        cutImage(read,rect);
        List<FaceInfo> faceInfoList2 = new ArrayList<FaceInfo>();
        int detectFacesResult2 = faceEngine.detectFaces(imageInfo.getRgbData(), imageInfo.getWidth(), imageInfo.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfoList2);
        System.out.println("detectFacesResult2:"+detectFacesResult2);
        //提取人脸特征
        FaceFeature faceFeature = new FaceFeature();
        faceEngine.extractFaceFeature(imageInfo.getRgbData(), imageInfo.getWidth(), imageInfo.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfoList.get(0), faceFeature);

        FaceFeature faceFeature2 = new FaceFeature();
        faceEngine.extractFaceFeature(imageInfo2.getRgbData(), imageInfo2.getWidth(), imageInfo2.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfoList2.get(0), faceFeature2);

        //人脸对比
        FaceFeature targetFaceFeature = new FaceFeature();
        targetFaceFeature.setFeatureData(faceFeature.getFeatureData());

        FaceFeature sourceFaceFeature = new FaceFeature();
        sourceFaceFeature.setFeatureData(faceFeature2.getFeatureData());

        FaceSimilar faceSimilar = new FaceSimilar();
        int compareFaceResult = faceEngine.compareFaceFeature(targetFaceFeature, sourceFaceFeature, faceSimilar);
        System.out.println("faceSimilar，compareFaceResult:"+compareFaceResult);
        System.out.println("faceSimilar，score:"+faceSimilar.getScore());
        int processResult = faceEngine.process(imageInfo.getRgbData(), imageInfo.getWidth(), imageInfo.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfoList, FunctionConfiguration.builder().supportAge(true).supportFace3dAngle(true).supportGender(true).supportLiveness(true).build());
        System.out.println("processResult:"+processResult);
        //性别提取
        List<GenderInfo> genderInfoList = new ArrayList<GenderInfo>();
        int genderCode = faceEngine.getGender(genderInfoList);
        System.out.println("genderCode:"+genderCode);
        System.out.println("genderInfoList:"+genderInfoList.get(0).getGender());
        //年龄提取
        List<AgeInfo> ageInfoList = new ArrayList<AgeInfo>();
        int ageCode = faceEngine.getAge(ageInfoList);
        System.out.println("ageCode:"+ageCode);
        System.out.println("ageInfoList:"+ageInfoList.get(0).getAge());
        //3D信息提取
        List<Face3DAngle> face3DAngleList = new ArrayList<Face3DAngle>();
        int face3dCode = faceEngine.getFace3DAngle(face3DAngleList);
        System.out.println("face3dCode:"+face3dCode);
        System.out.println("pitch:"+face3DAngleList.get(0).getPitch());//仰望角(上下)
        System.out.println("roll:"+face3DAngleList.get(0).getRoll());//横滚角(翻滚)
        System.out.println("yaw:"+face3DAngleList.get(0).getYaw());//偏航角 (左右)
        //活体信息
        List<LivenessInfo> livenessInfoList = new ArrayList<LivenessInfo>();
        int livenessCode = faceEngine.getLiveness(livenessInfoList);
        System.out.println("livenessCode:"+livenessCode);
        System.out.println("livenessInfoList:"+livenessInfoList.get(0).getLiveness());
    }

    public ImageInfo getRGBData(File file) {
        if (file == null){
            return null;
        }
        ImageInfo imageInfo;
        try {
            //将图片文件加载到内存缓冲区
            BufferedImage image = ImageIO.read(file);
            imageInfo = bufferedImage2ImageInfo(image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return imageInfo;
    }

    private ImageInfo bufferedImage2ImageInfo(BufferedImage image) {
        ImageInfo imageInfo = new ImageInfo();
        int width = image.getWidth();
        int height = image.getHeight();
        // 使图片居中
        width = width & (~3);
        height = height & (~3);
        imageInfo.setWidth(width);
        imageInfo.setHeight(height);
        //根据原图片信息新建一个图片缓冲区
        BufferedImage resultImage = new BufferedImage(width, height, image.getType());
        //得到原图的rgb像素矩阵
        int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);
        //将像素矩阵 绘制到新的图片缓冲区中
        resultImage.setRGB(0, 0, width, height, rgb, 0, width);
        //进行数据格式化为可用数据
        BufferedImage dstImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        if (resultImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
            ColorConvertOp colorConvertOp = new ColorConvertOp(cs, dstImage.createGraphics().getRenderingHints());
            colorConvertOp.filter(resultImage, dstImage);
        } else {
            dstImage = resultImage;
        }
        //获取rgb数据
        imageInfo.setRgbData(((DataBufferByte) (dstImage.getRaster().getDataBuffer())).getData());
        return imageInfo;
    }

    static void cutImage(BufferedImage image,Rect rect){
        Rectangle rectangle = new Rectangle();
        rectangle.setRect(rect.getLeft(),rect.getTop(),rect.getRight()-rect.getLeft(),rect.getBottom()-rect.getTop());
        BufferedImage bufferedImage = ImageUtil.cut(image, rectangle);
        try {
            ImageUtil.writeJpg(bufferedImage,new FileOutputStream(new File("C:\\Users\\huangsq\\Desktop\\a\\face"+System.currentTimeMillis()+".jpg")));
        }catch (Exception e){
           e.printStackTrace();
        }
    }

    class ImageInfo {
        public byte[] rgbData;
        public int width;
        public int height;

        public byte[] getRgbData() {
            return rgbData;
        }

        public void setRgbData(byte[] rgbData) {
            this.rgbData = rgbData;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}
