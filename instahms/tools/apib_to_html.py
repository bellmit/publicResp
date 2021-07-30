import os
import sys
import subprocess
import shutil

def process_dir(source, dest):
    for source_subdir, dirs, files in os.walk(source):
        dest_subdir = source_subdir.replace(source, dest)
        if not os.path.isdir(dest_subdir):
            os.makedirs(dest_subdir)
        template_file = os.path.join(source, "template.html")
        for file in files:
            source_file = os.path.join(source_subdir, file)
            dest_file = os.path.join(dest_subdir, file.replace(".apib", ".html"))
            if file.endswith(".png"):
                subprocess.call(['cp', source_file, dest_file])
                continue
            if not file.endswith(".apib"):
                continue
            source_file = os.path.join(source_subdir, file)
            dest_file = os.path.join(dest_subdir, file.replace(".apib", ".html"))
            subprocess.call(['snowboard', 'html', '-o', dest_file, \
                           "-t", template_file, source_file])
            print("Created :", dest_file)


if __name__ == '__main__':
    if len(sys.argv) < 3:
        print("Usage :", sys.argv[0], "<input_folder>", "<output_folder>")
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
