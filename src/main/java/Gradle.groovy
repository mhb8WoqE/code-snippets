File download(String url, String path) {
    def filePath = project.file(path).toPath()
    if (java.nio.file.Files.notExists(filePath)) {
        println 'downloading ' + path
        java.nio.file.Files.createDirectories(filePath.getParent())
        def c = new URL(url).openConnection()
        java.nio.file.Files.copy(c.inputStream, filePath)
    }
    return filePath.toFile()
}

// =====================

println('Java: ' + System.getProperty('java.version') + ' JVM: ' + System.getProperty('java.vm.version') + '(' + System.getProperty('java.vendor') + ') Arch: ' + System.getProperty('os.arch'))
