from cssjanus import ChangeLeftToRightToLeft
import os
import sys
import codecs
import shutil

def process_dir(source, dest):
    for source_subdir, dirs, files in os.walk(source):
        dest_subdir = source_subdir.replace(source, dest)
        if not os.path.isdir(dest_subdir):
            os.makedirs(dest_subdir)
        for file in files:
            if not file.endswith(".css"):
                continue
            source_file = os.path.join(source_subdir, file)
            dest_file = os.path.join(dest_subdir, file)
            with codecs.open(dest_file, 'w', encoding='utf-8') as outfile, \
                codecs.open(source_file, 'r', encoding='utf-8') as infile:
                fixed_lines = ChangeLeftToRightToLeft(infile.readlines())
                outfile.write(''.join(fixed_lines))
            print("Created :", dest_file)


if __name__ == '__main__':
    if len(sys.argv) < 3:
        print("Usage :", sys.argv[0], "<input_folder>", "<output_folder>", "[-e]")
        print("-e\t\t Empty destination folder if exists")
        sys.exit(1)
    source = sys.argv[1]
    dest = sys.argv[2]
    if not os.path.isdir(source):
        print("Source Error:", source, "is not a directory")
        sys.exit(1)
    if os.path.exists(dest) and not os.path.isdir(dest):
        print("Destination Error:", dest, "is not a directory")
        sys.exit(1)
    if source == dest:
        print("Destination Error:", dest, "can not be same as source", source)
        sys.exit(1)
    if os.path.realpath(source) + "/" in os.path.realpath(dest):
        print("Destination Error:", dest, "is a subdirectory of source", source)
        sys.exit(1)
    flush_dest = False
    if '-e' in sys.argv:
        flush_dest = True
    if flush_dest and os.path.isdir(dest):
        shutil.rmtree(dest, ignore_errors=True)
    if not os.path.exists(dest):
        os.makedirs(dest)
    process_dir(os.path.realpath(source), os.path.realpath(dest))
