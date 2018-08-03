# VeraGit

A CLI tool to automatically submit Github repos to the Veracode platform for scanning.
Supports all non-compilable languages that only require zipping the source files.

Commands:

Creates a app profile for easy rescanning: create --id veracode_app_id --name name_for_the_profile --url the_link_to_the_.git_url --key github_oath_key(optional, required for private repositories) --public (use if repository is public)

scans a url without an app profile: scan --url the_link_to_the_.git_url --id veracode_app_id --key github_oath_key(optional, required for private repositories) --public (use if repository is public)

scan an app profile: scan --app the_name_of_the_app_profile_you_previously_created

The first run will require you give your Veracode API credentials to allow API scanning.


Required libraries:
JGit
Veracode API
argparse4j
yamlbeans
slf4j
jsch
javax.xml.bind
