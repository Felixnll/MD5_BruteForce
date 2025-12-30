#!/bin/bash
echo "========================================"
echo "  Building MD5 BruteForce Project"
echo "========================================"
echo ""

# Create target directory if it doesn't exist
mkdir -p target/classes

# Check if javac exists
if ! command -v javac &> /dev/null; then
    echo "ERROR: Java JDK not found!"
    echo ""
    echo "Install Java JDK with:"
    echo "  Ubuntu/Debian: sudo apt install default-jdk"
    echo "  Fedora:        sudo dnf install java-17-openjdk-devel"
    echo "  Arch:          sudo pacman -S jdk-openjdk"
    exit 1
fi

echo "Using Java from: $(which javac)"
echo ""

# Compile all Java files
javac -sourcepath src/main/java -d target/classes \
    src/main/java/server/RMIServer.java \
    src/main/java/client/BruteForceClient.java \
    src/main/java/util/TestHashGenerator.java

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "  Build successful!"
    echo "========================================"
    echo ""
    echo "You can now run:"
    echo "  ./start-server-1.sh"
    echo "  ./start-server-2.sh"
    echo "  ./start-client.sh"
    echo ""
else
    echo ""
    echo "========================================"
    echo "  Build FAILED!"
    echo "========================================"
    echo ""
fi
