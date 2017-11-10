Add-Type -A System.IO.Compression.FileSystem
rm .\CopyPasta.zip
[IO.Compression.ZipFile]::CreateFromDirectory('.\classes\artifacts\CopyPasta', 'CopyPasta.zip')
#[IO.Compression.ZipFile]::ExtractToDirectory('foo.zip', 'bar')
#mv .\CopyPasta.zip .\classes\artifacts\
#pause