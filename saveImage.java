 private void saveImage() {
        mFrameSave.setDrawingCacheEnabled(true);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        Bitmap currImage = mFrameSave.getDrawingCache();
        currImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String folder_main = "Frame Collage";
        final File f0 = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), folder_main);
        if (!f0.exists()) {
            f0.mkdirs();
        }
        final String path = f0.getPath() + File.separator + imageFileName + ".jpg";
        file = new File(path);
        try {
            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaStore.Images.Media.insertImage(getContentResolver(), currImage, "", "");
        saved = true;
        MediaScannerConnection.scanFile(FrameActivity.this, new String[]{f0.toString()}, null, null);
        if (!sharing) {
            showDialog(VarHolder.DIALOG_VIEW);
            sharing = false;
        }
    }