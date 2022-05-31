# Extract text from each PDF file using PDFBox
$FILE_PATH=Get-ChildItem -Path .\output
forEach ($f IN $FILE_PATH) {
    Write-Output "Extracting Text: $f"
    java -jar .\PDFBOX\pdfbox-app-2.0.25.jar ExtractText .\output\$f
}