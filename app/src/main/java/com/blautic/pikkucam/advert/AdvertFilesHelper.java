package com.blautic.pikkucam.advert;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jsf on 9/09/16.
 */

public class AdvertFilesHelper {

    private List<AdvertFile> advertList;
    private String path;
    private File defFile;
    int reqWidth=0;
    int reqHeight=0;
    private String marcavideo="watermark";


    public AdvertFilesHelper(String path)
    {
        this.path = path;
        defFile = new File(path);
        createFileList();
    }

    public void setSize(int w,int h)
    {
        reqWidth=w;
        reqHeight=h;
    }

    public void createFileList()
    {
        AdvertFile advfile;

        advertList = new ArrayList<AdvertFile>();

        File[] files = defFile.listFiles(new imgFilter());

        if(files != null)
        {
            advertList.clear();

            for(File file : files){
                advfile = new AdvertFile();
                advfile.setName(file.getName());
                advertList.add(advfile);
            }

        }

    }

    public List<AdvertFile> getFileList(){

        return advertList;
    }

    public String[] getFileArray()
    {
        String[] stArray = new String[advertList.size()];
        for(int i=0;i<advertList.size();i++)
        {
            stArray[i]=advertList.get(i).getName();
        }

        return stArray;
    }

    private class imgFilter implements FileFilter {
        private String[] extension ={"png","jpg"};

        @Override
        public boolean accept(File pathname){
            if (pathname.isDirectory()){ return false; }
            String name = pathname.getName().toLowerCase();
            if(!name.contains(marcavideo)) {
                for (String anExt : extension) {
                    if (name.endsWith(anExt)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public AdvertFile copy(File src) throws IOException {
        InputStream in = new FileInputStream(src);
        File defFile = new File(path);
        String path = defFile.getPath()+java.io.File.separator+src.getName();
        OutputStream out = new FileOutputStream(path);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

        AdvertFile af = new AdvertFile();
        af.setName(src.getName());
        advertList.add(af);
        return af;
    }

    public Bitmap getImage(String name)
    {
        Bitmap bitmap;
        String filedata = path+java.io.File.separator+name;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        if(reqHeight!=0 || reqWidth !=0)
        {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filedata, options);


            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
        }
        options.inJustDecodeBounds = false;
        bitmap= BitmapFactory.decodeFile(filedata,options);
        return bitmap;
    }

    public AdvertFile deleteSelectedImage(AdvertFile af)
    {
        String filedata = path+java.io.File.separator+af.getName();
        File file = new File(filedata);
        if(file.exists()) file.delete();
        advertList.remove(af);

        if(advertList.size() > 0 ) {
            return advertList.get(0);
        }else return null;
    }

    public int getIndexAdvertFile(String name)
    {
        for(AdvertFile af:advertList)
        {
            if(af.getName().contentEquals(name)) return advertList.indexOf(af);
        }

        return 0;
    }

    public String getNameFromIdx(int idx)
    {
        if(idx < advertList.size()) return advertList.get(idx).getName();
        else return advertList.get(0).getName();
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
