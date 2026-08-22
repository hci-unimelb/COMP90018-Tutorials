#!/usr/bin/env python3
"""Convert every non-ASCII character in an HTML file to a numeric HTML entity.

Why this exists: a bare `python3 -m http.server` (the quickest way to preview
a local HTML file) sends `Content-Type: text/html` with no charset parameter.
Browsers then guess the encoding, often landing on Latin-1/Windows-1252,
which mangles em-dashes, curly quotes, and emoji into mojibake (e.g. "\xe2\x80\x94"
instead of an em dash). Converting every non-ASCII character to a numeric
entity (`&#x2014;` instead of the raw byte sequence) makes the page render
correctly no matter how it's served or what charset a viewer assumes.

This is safe to run repeatedly (idempotent) since entities are pure ASCII
and won't be re-encoded.

Usage: python3 entity_safe.py <path-to-html-file>
"""
import sys


def main():
    if len(sys.argv) != 2:
        print("Usage: python3 entity_safe.py <path-to-html-file>", file=sys.stderr)
        sys.exit(1)

    path = sys.argv[1]
    with open(path, encoding="utf-8") as f:
        text = f.read()

    out = []
    for ch in text:
        if ord(ch) > 127:
            out.append("&#x{:X};".format(ord(ch)))
        else:
            out.append(ch)
    converted = "".join(out)

    with open(path, "w", encoding="utf-8") as f:
        f.write(converted)

    remaining = sum(1 for c in converted if ord(c) > 127)
    print(f"Converted {path}. Remaining non-ASCII characters: {remaining}")


if __name__ == "__main__":
    main()
