import java.net.URL;
import java.net.URLClassLoader;

public class DriverShimClassLoader extends URLClassLoader {
    public DriverShimClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public void addURL(URL url) {
        super.addURL(url);
    }
}