import hashlib
import os
import subprocess

def create_hashes(fname):
    hash_md5 = hashlib.md5()
    hash_sha1 = hashlib.sha1()
    with open(fname, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
            hash_sha1.update(chunk)
    with open(fname + ".md5" , "w") as f:
    	f.write(hash_md5.hexdigest())
    with open(fname + ".sha1" , "w") as f:
    	f.write(hash_sha1.hexdigest())

dir_path = os.path.dirname(os.path.realpath(__file__))[:-14] + "maven-private-repo"

for ext in ["xml", "jar", "pom"]:
	files = subprocess.check_output("find %s -name '*.%s'" % (dir_path, ext), shell=True).strip("\n ").split("\n")
	for f in files:
		create_hashes(f.strip())

