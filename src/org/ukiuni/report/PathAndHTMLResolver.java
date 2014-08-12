package org.ukiuni.report;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

public class PathAndHTMLResolver {
	public static PathAndHTMLResolver instance;

	private PathAndHTMLResolver() {
	}

	public List<PathAndHtml> resolve(ServletContext context) {
		File templatesDirectory = new File(context.getRealPath("template"));
		File webRootDirectory = new File(context.getRealPath("/"));
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().endsWith(".html") || file.getName().endsWith(".htm");
			}
		};
		List<PathAndHtml> pathAndHtmls = new ArrayList<PathAndHtml>();
		int parentPathLength = webRootDirectory.getAbsolutePath().length();
		for (File templateFile : templatesDirectory.listFiles(filter)) {
			String fileName = templateFile.getName();
			String path = "/" + fileName.substring(0, fileName.lastIndexOf("."));
			if (path.endsWith("index")) {
				path = path.substring(0, path.length() - "index".length());
			}
			String canonicalPath = templateFile.getAbsolutePath().substring(parentPathLength + 1).replace(File.pathSeparator, "/");
			pathAndHtmls.add(new PathAndHtml(path, canonicalPath));
		}
		return pathAndHtmls;
	}

	public static class PathAndHtml {
		public PathAndHtml(String path, String html) {
			super();
			this.path = path;
			this.html = html;
		}

		public String path;
		public String html;
	}

	public static PathAndHTMLResolver instance() {
		if (null == instance) {
			synchronized (PathAndHTMLResolver.class) {
				if (null == instance) {
					instance = new PathAndHTMLResolver();
				}
			}
		}
		return instance;
	}
}
