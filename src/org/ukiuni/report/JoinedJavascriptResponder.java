package org.ukiuni.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class JoinedJavascriptResponder implements Filter {
	private File joinJavaScriptDirectory;
	private File rootDirectory;
	private boolean compress;

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse response, FilterChain arg2) throws IOException, ServletException {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".js");
			}
		};
		response.setContentType("text/javascript");
		BufferedWriter writer = new BufferedWriter(response.getWriter());
		for (File childs : joinJavaScriptDirectory.listFiles(filter)) {
			Reader reader = new FileReader(childs);

			final String localFilename = childs.getName();
			if (compress) {
				JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new ErrorReporter() {
					public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
						System.err.println("\n[WARNING] in " + localFilename);
						if (line < 0) {
							System.err.println("  " + message);
						} else {
							System.err.println("  " + line + ':' + lineOffset + ':' + message);
						}
					}

					public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
						System.err.println("[ERROR] in " + localFilename);
						if (line < 0) {
							System.err.println("  " + message);
						} else {
							System.err.println("  " + line + ':' + lineOffset + ':' + message);
						}
					}

					public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
						error(message, sourceName, line, lineSource, lineOffset);
						return new EvaluatorException(message);
					}
				});
				compressor.compress(writer, -1, true, false, false, true);
				writer.append(";");
			} else {
				String javaScriptPath = childs.getAbsolutePath().substring(rootDirectory.getAbsolutePath().length() + 1).replace(File.pathSeparator, "/");
				writer.write("document.write(\'<script type=\"text/javascript\" language=\"javascript\" src=\"" + javaScriptPath + "\" /></script>\');");
			}
		}
		writer.flush();
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		joinJavaScriptDirectory = new File(config.getServletContext().getRealPath("scripts/controllers"));
		rootDirectory = new File(config.getServletContext().getRealPath("/"));
		compress = "true".equals(config.getInitParameter("compress"));
	}
}
