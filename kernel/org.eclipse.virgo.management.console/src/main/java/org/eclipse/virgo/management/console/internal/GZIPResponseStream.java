package org.eclipse.virgo.management.console.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

public class GZIPResponseStream extends ServletOutputStream {

	private ByteArrayOutputStream byteStream = null;

	private GZIPOutputStream gzipStream = null;

	private boolean closed = false;

	private HttpServletResponse response = null;

	private ServletOutputStream servletStream = null;

	public GZIPResponseStream(HttpServletResponse response) throws IOException {
		super();
		closed = false;
		this.response = response;
		this.servletStream = response.getOutputStream();
		byteStream = new ByteArrayOutputStream();
		gzipStream = new GZIPOutputStream(byteStream);
	}

	public void close() throws IOException {
		if (closed) {
			throw new IOException("This output stream has already been closed");
		}
		gzipStream.finish();

		byte[] bytes = byteStream.toByteArray();

		response.setContentLength(bytes.length);
		try {
			response.addHeader("Content-Encoding", "gzip");
		} catch(UnsupportedOperationException e){
			response.setHeader("Content-Encoding", "gzip"); //Not ideal as any previous content headers will be removed but none are set by the admin system.
		}
		servletStream.write(bytes);
		servletStream.flush();
		servletStream.close();
		closed = true;
	}

	public void flush() throws IOException {
		if (closed) {
			throw new IOException("Cannot flush a closed output stream");
		}
		gzipStream.flush();
	}

	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		gzipStream.write((byte) b);
	}

	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte b[], int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		gzipStream.write(b, off, len);
	}

    @Override
    public boolean isReady() {
        // TODO implement javax.servlet 3.1 method
        return false;
    }

    @Override
    public void setWriteListener(WriteListener arg0) {
        // TODO implement javax.servlet 3.1 method
    }
}