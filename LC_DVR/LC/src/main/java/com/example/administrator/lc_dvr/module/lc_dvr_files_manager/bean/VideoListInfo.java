package com.example.administrator.lc_dvr.module.lc_dvr_files_manager.bean;

import java.util.List;

/**
 * 返回多个数据时的实体类
 * Created by Administrator on 2016/7/19.
 */
public class VideoListInfo {

    private LISTBean LIST;

    public LISTBean getLIST() {
        return LIST;
    }

    public void setLIST(LISTBean LIST) {
        this.LIST = LIST;
    }

    public static class LISTBean {
        /**
         * File : {"TIME":"19/07/2016 15:56:36","TIMECODE":1223917330,"NAME":"2016_0719_155355_005.MOV","ATTR":32,"SIZE":224734356,"FPATH":"A:\\CARDV\\MOVIE\\2016_0719_155355_005.MOV"}
         */

        private List<ALLFileBean> ALLFile;

        public List<ALLFileBean> getALLFile() {
            return ALLFile;
        }

        public void setALLFile(List<ALLFileBean> ALLFile) {
            this.ALLFile = ALLFile;
        }

        public static class ALLFileBean {
            /**
             * TIME : 2018/10/15 10:54:49
             * TIMECODE : 1223917330
             * NAME : 2016_0719_155355_005.MOV
             * ATTR : 32
             * SIZE : 224734356
             * FPATH : A:\CARDV\MOVIE\2016_0719_155355_005.MOV
             */

            private FileBean File;

            public FileBean getFile() {
                return File;
            }

            public void setFile(FileBean File) {
                this.File = File;
            }

            public static class FileBean {
                private String TIME;
                private int TIMECODE;
                private String NAME;
                private int ATTR;
                private int SIZE;
                private String FPATH;

                public String getTIME() {
                    return TIME;
                }

                public void setTIME(String TIME) {
                    this.TIME = TIME;
                }

                public int getTIMECODE() {
                    return TIMECODE;
                }

                public void setTIMECODE(int TIMECODE) {
                    this.TIMECODE = TIMECODE;
                }

                public String getNAME() {
                    return NAME;
                }

                public void setNAME(String NAME) {
                    this.NAME = NAME;
                }

                public int getATTR() {
                    return ATTR;
                }

                public void setATTR(int ATTR) {
                    this.ATTR = ATTR;
                }

                public int getSIZE() {
                    return SIZE;
                }

                public void setSIZE(int SIZE) {
                    this.SIZE = SIZE;
                }

                public String getFPATH() {
                    return FPATH;
                }

                public void setFPATH(String FPATH) {
                    this.FPATH = FPATH;
                }
            }
        }
    }

}
