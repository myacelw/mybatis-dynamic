import re
import os
import sys

def slugify(text):
    text = text.lower().strip()
    text = re.sub(r'[^\w\s-]', '', text)
    text = re.sub(r'[-\s]+', '-', text)
    return text

def split_markdown(file_path, output_dir):
    print(f"Splitting {file_path} into {output_dir}")
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    current_title = "index"
    current_content = []
    
    # Check if first line is H1
    if lines and lines[0].startswith('# '):
        # We keep the H1 for index.md
        current_content.append(lines[0])
        lines = lines[1:]
    else:
        # No H1? Add one based on filename maybe? No, let's just proceed.
        pass

    for line in lines:
        if line.startswith('## '):
            # Save previous section
            if current_content:
                filename = "index.md" if current_title == "index" else f"{slugify(current_title)}.md"
                out_path = os.path.join(output_dir, filename)
                print(f"  -> Writing {out_path}")
                with open(out_path, 'w', encoding='utf-8') as f:
                    f.write("".join(current_content))
            
            # Start new section
            current_title = line.strip().lstrip('#').strip()
            # Promote H2 (##) to H1 (#) for the new page
            current_content = [f"# {current_title}\n"]
        else:
            current_content.append(line)

    # Save the last section
    if current_content:
        filename = "index.md" if current_title == "index" else f"{slugify(current_title)}.md"
        out_path = os.path.join(output_dir, filename)
        print(f"  -> Writing {out_path}")
        with open(out_path, 'w', encoding='utf-8') as f:
            f.write("".join(current_content))

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python3 split_md.py <input_file> <output_dir>")
        sys.exit(1)
    
    split_markdown(sys.argv[1], sys.argv[2])
