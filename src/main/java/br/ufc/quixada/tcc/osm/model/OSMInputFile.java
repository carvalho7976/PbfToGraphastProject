package br.ufc.quixada.tcc.osm.model;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;


import br.ufc.quixada.tcc.readerbasedOnOsmosis.PbfReader;
import br.ufc.quixada.tcc.readerbasedOnOsmosis.Sink;

public class OSMInputFile implements Sink, Closeable{

	private boolean eof;
	private InputStream bis;
	private boolean binary = false;
	private final BlockingQueue<GenericOsmElement> itemQueue;
	private boolean hasIncomingData;
	private int workerThreads = -1;
	Thread pbfReaderThread;


	public OSMInputFile( File file ) throws IOException {
		bis = decode(file);
		itemQueue = new LinkedBlockingQueue<GenericOsmElement>(50000);
	}
	public OSMInputFile open()    {
		openPBFReader(bis);
		return this;
	}
	private void openPBFReader( InputStream stream )    {
		
		hasIncomingData = true;
		if (workerThreads <= 0)
			workerThreads = 8;

		PbfReader reader = new PbfReader(stream, this, workerThreads);
		pbfReaderThread = new Thread(reader, "PBF Reader");
		pbfReaderThread.start();
	}

	
	public void process(GenericOsmElement item) {
		try {

			itemQueue.put(item);
		} catch (InterruptedException ex){
			throw new RuntimeException(ex);
		}

	}

	public void complete() {
		hasIncomingData = false;

	}

	public void close() throws IOException {
		eof = true;
		bis.close();
		if (pbfReaderThread != null && pbfReaderThread.isAlive())
			pbfReaderThread.interrupt();
	}


	private InputStream decode( File file ) throws IOException{

        final String name = file.getName();

        InputStream ips = null;
        try
        {
            ips = new BufferedInputStream(new FileInputStream(file), 50000);
        } catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        ips.mark(10);

        // check file header
        byte header[] = new byte[6];
        if (ips.read(header) < 0)
            throw new IllegalArgumentException("Input file is not of valid type " + file.getPath());

        /*     can parse bz2 directly with additional lib
         if (header[0] == 'B' && header[1] == 'Z')
         {
         return new CBZip2InputStream(ips);
         }
         */
        if (header[0] == 31 && header[1] == -117)
        {
            ips.reset();
            return new GZIPInputStream(ips, 50000);
        } else if (header[0] == 0 && header[1] == 0 && header[2] == 0
                && header[4] == 10 && header[5] == 9
                && (header[3] == 13 || header[3] == 14))
        {
            ips.reset();
            binary = true;
            return ips;
        } else if (header[0] == 'P' && header[1] == 'K')
        {
            ips.reset();
            ZipInputStream zip = new ZipInputStream(ips);
            zip.getNextEntry();

            return zip;
        } else if (name.endsWith(".osm") || name.endsWith(".xml"))
        {
            ips.reset();
            return ips;
        } else if (name.endsWith(".bz2") || name.endsWith(".bzip2"))
        {
            String clName = "org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream";
            try
            {
                Class clazz = Class.forName(clName);
                ips.reset();
                Constructor<InputStream> ctor = clazz.getConstructor(InputStream.class, boolean.class);
                return ctor.newInstance(ips, true);
            } catch (Exception e)
            {
                throw new IllegalArgumentException("Cannot instantiate " + clName, e);
            }
        } else
        {
            throw new IllegalArgumentException("Input file is not of valid type " + file.getPath());
        }
	}
	public OSMInputFile setWorkerThreads( int num )	{
		workerThreads = num;
		return this;
	}
	
	public GenericOsmElement getNext(){
		  GenericOsmElement item;
	       
	       item = getNextPBF();
	      
	       if (item != null)
	            return item;

	        eof = true;
	        return null;
	 }
	  private GenericOsmElement getNextPBF() {
		  
	        GenericOsmElement next = null;
	        while (next == null)
	        {
	            if (!hasIncomingData && itemQueue.isEmpty())
	            {
	                // we are done, stop polling
	                eof = true;
	                break;
	            }

	            try
	            {
	                // we cannot use "itemQueue.take()" as it blocks and hasIncomingData can change
	                next = itemQueue.poll(10, TimeUnit.MILLISECONDS);
	            } catch (InterruptedException ex)
	            {
	                eof = true;
	                break;
	            }
	        }
	        return next;
	    }
	 
	public boolean isEOF(){
	        return eof;
	    }
	


}
