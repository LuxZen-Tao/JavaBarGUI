import pathlib

repls = {
    '£': 'GBP ',
    '–': '-', '—': '-', '−': '-', '…': '...',
    '’': "'", '‘': "'", '“': '"', '”': '"',
    '•': '*',
}

def clean(text: str) -> str:
    for a, b in repls.items():
        text = text.replace(a, b)
    return ''.join(ch if ord(ch) < 128 else '' for ch in text)

root = pathlib.Path('.')
changed = []
for p in root.glob('*.java'):
    txt = p.read_bytes().decode('utf-8', errors='replace')
    new = clean(txt)
    if new != txt:
        p.write_text(new, encoding='utf-8')
        changed.append(p.name)

print('cleaned', len(changed), 'files')
for n in changed:
    print(' -', n)
