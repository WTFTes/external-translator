# External translator machine translation plugin for OmegaT

This is machine translation plugin for OmegaT. It allows calling third-party executable to get translation.

## Building

```
./gradlew jar 
```

## Install

Place jar into OmegaT plugins directory.

Enable 'External translator' machine translation. Edit translation settings.

E.g.:

Command: `python.exe`

Arguments: `c:\scripts\translate.py {src_lang} {dst_lang} {text}`

* OmegaT will replace templated arguments with corresponding strings - source language code, destination language code and base64-encoded UTF-8 text.
* OmegaT will read result of translation from standard output, program must exit with code 0 on success and result output must be base64-encoded UTF-8 string.