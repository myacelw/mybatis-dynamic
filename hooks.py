import os
import shutil
import logging

log = logging.getLogger("mkdocs")

def on_post_build(config, **kwargs):
    site_dir = config["site_dir"]
    docs_dir = config["docs_dir"]
    
    # 1. Ensure i18n-nav.js exists in both root and zh/js/
    # We copy from source docs/js/i18n-nav.js to be sure
    src_js = os.path.join(docs_dir, "js", "i18n-nav.js")
    
    dest_root_js = os.path.join(site_dir, "js", "i18n-nav.js")
    dest_zh_js = os.path.join(site_dir, "zh", "js", "i18n-nav.js")
    
    if os.path.exists(src_js):
        # Ensure root
        if not os.path.exists(dest_root_js):
            os.makedirs(os.path.dirname(dest_root_js), exist_ok=True)
            shutil.copy(src_js, dest_root_js)
            log.info(f"Hook: Restored missing {dest_root_js}")
        
        # Ensure zh
        os.makedirs(os.path.dirname(dest_zh_js), exist_ok=True)
        shutil.copy(src_js, dest_zh_js)
        log.info(f"Hook: Copied i18n-nav.js to {dest_zh_js}")
    else:
        log.warning(f"Hook: Source JS not found at {src_js}")

    # 2. Duplicate sitemap.xml to zh/sitemap.xml
    src_sitemap = os.path.join(site_dir, "sitemap.xml")
    dest_zh_sitemap = os.path.join(site_dir, "zh", "sitemap.xml")
    
    if os.path.exists(src_sitemap):
        os.makedirs(os.path.dirname(dest_zh_sitemap), exist_ok=True)
        shutil.copy(src_sitemap, dest_zh_sitemap)
        log.info(f"Hook: Duplicated sitemap.xml to {dest_zh_sitemap}")
    else:
        log.warning(f"Hook: Root sitemap.xml not found at {src_sitemap}")
