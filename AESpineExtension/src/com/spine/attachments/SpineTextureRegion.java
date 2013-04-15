package com.spine.attachments;

import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.region.TextureRegion;


public class SpineTextureRegion extends TextureRegion {
	
	public String mName;
	
	/** The offset from the left of the original image to the left of the packed image, after whitespace was removed for packing. */
	public float offsetX;

	/** The offset from the bottom of the original image to the bottom of the packed image, after whitespace was removed for
	 * packing. */
	public float offsetY;

	/** The width of the image, after whitespace was removed for packing. */
	public int packedWidth;

	/** The height of the image, after whitespace was removed for packing. */
	public int packedHeight;

	/** The width of the image, before whitespace was removed and rotation was applied for packing. */
	public int originalWidth;

	/** The height of the image, before whitespace was removed for packing. */
	public int originalHeight;
	
	public SpineTextureRegion(final String pName,final ITexture pTexture, final float pTextureX, final float pTextureY, final float pTextureWidth, final float pTextureHeight, final float pScale, final boolean pRotated) {
		super(pTexture, pTextureX, pTextureY, pTextureWidth, pTextureHeight, pScale, pRotated);
		mName = pName;
	}
	
}