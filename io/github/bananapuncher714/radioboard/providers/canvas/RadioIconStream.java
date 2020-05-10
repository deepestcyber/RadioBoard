package io.github.bananapuncher714.radioboard.providers.canvas;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.bukkit.entity.Entity;

import io.github.bananapuncher714.radioboard.BoardFrame;
import io.github.bananapuncher714.radioboard.api.DisplayInteract;
import io.github.bananapuncher714.radioboard.util.GifDecoder;
import io.github.bananapuncher714.radioboard.util.GifDecoder.GifImage;
import io.github.bananapuncher714.radioboard.util.JetpImageUtil;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

/**
 * Plays a gif
 * 
 * @author BananaPuncher714
 */
public class RadioIconStream implements RadioIcon {
	private volatile boolean RUNNING = true;
	
	protected int width;
	protected int height;
		
	protected RadioCanvas canvas;
	
	protected EmbeddedMediaPlayer mediaPlayer;
	
	private final class TestBufferFormatCallback extends BufferFormatCallbackAdapter {

	    @Override
	    public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
	        return new RV32BufferFormat(width, height);
	    }

	}
	
    private final int[] rgbBuffer;
    
    private BufferedImage outputBufferImage;
    private int[] outputBuffer;
	
    private final class TestRenderCallback implements RenderCallback {

        // This is not optimal, see the CallbackMediaPlayerComponent for a way to render directly into the raster of a
        // Buffered image
    	
    	long lastUpdated;
    	
    	final long frameDelay = 1/30 * 10;

        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            ByteBuffer bb = nativeBuffers[0];
            IntBuffer ib = bb.asIntBuffer();
            ib.get(rgbBuffer);
			
			outputBufferImage.setRGB(0,  0,  width, height, rgbBuffer, 0, width);
			outputBuffer = JetpImageUtil.getRGBArray(outputBufferImage);
			
        	if ( canvas != null ) {
				canvas.update( RadioIconStream.this );
			}
        	
			try {
				// Make up time lost for sending an update to the client
				//Thread.sleep( Math.max( 0, frameDelay - ( System.currentTimeMillis() - lastUpdated ) ) );
				Thread.sleep(10);
			} catch ( InterruptedException e ) {
				e.printStackTrace();
			}
			lastUpdated = System.currentTimeMillis();
        }
    }
	
	public RadioIconStream( String url, int width, int height ) {
		rgbBuffer = new int[width * height];
		
		try {
			this.width = width;
			this.height = height;			
			
			outputBufferImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
			
	        MediaPlayerFactory factory = new MediaPlayerFactory();
	        mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
	        mediaPlayer.videoSurface().set(factory.videoSurfaces().newVideoSurface(
	        		new TestBufferFormatCallback(), 
	        		new TestRenderCallback(), 
	        		true));
	        mediaPlayer.media().play(url);
			
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public int[] getDisplay() {
		return outputBuffer;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void init( RadioCanvas provider ) {
		canvas = provider;
	}

	@Override
	public void onClick( BoardFrame frame, Entity entity, DisplayInteract action, int x, int y) {
	}

	@Override
	public void terminate() {
		RUNNING = false;
		canvas = null;
	}
}


