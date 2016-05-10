package br.ufc.quixada.tcc.osm.model;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

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
			workerThreads = 2;

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
		InputStream ips = null;
		try{
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

		if (header[0] == 31 && header[1] == -117){
			ips.reset();
			return new GZIPInputStream(ips, 50000);
		} else if (header[0] == 0 && header[1] == 0 && header[2] == 0
				&& header[4] == 10 && header[5] == 9
				&& (header[3] == 13 || header[3] == 14)){
			ips.reset();
			binary = true;
			return ips;
		} else{
			throw new IllegalArgumentException("Input file is not of valid type " + file.getPath());
		}
	}
	public OSMInputFile setWorkerThreads( int num )	{
		workerThreads = num;
		return this;
	}
	
	public GenericOsmElement getNext(){
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


}
