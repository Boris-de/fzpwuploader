import org.jspecify.annotations.NullMarked;

@NullMarked
module de.achterblog.fzpwuploader {
  requires java.desktop;
  requires java.net.http;

  requires static lombok;
  requires static org.jspecify;
}
