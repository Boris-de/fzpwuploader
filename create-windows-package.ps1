# == Packaging instructions for Windows ==
# * Install compatible version of openjdk (must be >=14 to support jpackage)
# * Install .NET Framework 3.5.1 (needed for WiX, https://www.microsoft.com/en-us/download/details.aspx?id=22)
# * Install WiX (https://wixtoolset.org/)
# * Start a powershell and run the following commands
#  Set-ExecutionPolicy -Scope Process Unrestricted
#  .\create-windows-package.ps1 .\path\to\release.jar version-number

$ErrorActionPreference = "Stop"

If ( $args.Length -eq 0 ) { echo "Missing parameter for JAR_FILE"; exit 1 }
If ( $args.Length -lt 2 ) { echo "Missing parameter for VERSION"; exit 1 }

$JAR_FILE = $args[0]
$VERSION = $args[1]
$VM_TYPE = $args[2]
$TARGET_DIR = ".\target"
$ICON_FILE = "$TARGET_DIR\icon.ico"
$LICENSE_FILE = "$TARGET_DIR\license.txt"
$RUNTIME = "$TARGET_DIR\runtime"
$APP_MODULE = "de.achterblog.fzpwuploader"

# default to "client" (but some JDKs only provide "server") 
If (!($VM_TYPE)) { $VM_TYPE = "client" }

If (${JAVA_HOME} -And (Test-Path $JAVA_HOME)) { $env:Path = "${JAVA_HOME};${env:Path}" }

If (Test-Path $RUNTIME) { echo "Cleaning up"; rm -Recurse $RUNTIME }
mkdir $TARGET_DIR -ErrorAction SilentlyContinue

echo "Generating license file"
Set-Content $LICENSE_FILE "fzpwuploader is released under the following license terms.`n"
Add-Content $LICENSE_FILE "(This license does not cover the application icon)`n"
Add-Content $LICENSE_FILE "(This license does not cover the application runtime, check the folder 'runtime/legal' in the installation folder for details on the runtime license)`n"
cat .\COPYING | Add-Content $LICENSE_FILE

If (!( Test-Path $ICON_FILE )) { echo "Fetching icon from fzpw"; Invoke-WebRequest -Uri https://freizeitparkweb.de/favicon.ico -OutFile $ICON_FILE }

echo "Generating runtime image"
jlink --module-path $JAR_FILE `
      --add-modules $APP_MODULE `
      --add-modules jdk.crypto.ec `
      --output $RUNTIME `
      --vm $VM_TYPE `
      --no-header-files `
      --strip-debug `
      --no-man-pages `
      --strip-native-commands
If (!( Test-Path $RUNTIME )) { echo "Failed to create runtime in $RUNTIME"; exit 1 }

echo "Generating installer for $VERSION"
jpackage -n fzpwuploader `
         -m $APP_MODULE/de.achterblog.fzpwuploader.Launcher `
         --runtime-image $RUNTIME `
         -t exe `
         --app-version $VERSION `
         --win-per-user-install `
         --win-menu `
         -d . `
         --icon $ICON_FILE `
         --license-file $LICENSE_FILE `
         --win-dir-chooser

echo "All done"
