# VeraGit

A CLI tool to automatically submit Github repos to the Veracode platform for scanning.
Support all non-compilable languages that only require zipping the source files.

Commands:

create --id veracode_app_id --name name_for_the_profile --url the_link_to_the_.git_url --key github_oath_key(optional, required for private repositories) --public (use if repository is public)

scan --url the_link_to_the_.git_url --id veracode_app_id --key github_oath_key(optional, required for private repositories) --public (use if repository is public)

scan --app the_name_of_the_app_profile_you_previously_created

The first run will require you give your Veracode API credentials to allow API scanning.
