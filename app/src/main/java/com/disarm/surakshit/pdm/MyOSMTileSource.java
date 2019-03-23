package com.disarm.surakshit.pdm;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

/**
 * Created by naman on 19/8/17.
 */

public class MyOSMTileSource extends OnlineTileSourceBase {

    public MyOSMTileSource(final String aName, final int aZoomMinLevel,
                        final int aZoomMaxLevel, final int aTileSizePixels, final String aImageFilenameEnding,
                        final String[] aBaseUrl) {
        super(aName, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
                aImageFilenameEnding, aBaseUrl);
    }

    @Override
    public String getTileURLString(long pMapTileIndex) {
        return null;
    }
}