**Cloud Sync:**

When setting up the Cloud Sync function for the first time, it is necessary to create the folder "Zinsen" in the Dropbox root directory and upload the file "*save.toml*" with a template into the folder:

*save.toml* Template

```toml
[tmp]
date=09.08.2026
method=2
interest_rate=40.0
money=10.0
```

In the next step, you will need the following values:

- App key
- App secret
You can obtain these by creating an app on "www.dropbox.com/developers/apps".

After creating the app, you must activate the following permissions:

- files.metadata.write
- files.content.write
- files.content.read

Once this is done, you only need to follow the steps provided by the program.

Sharing:

To use Cloud Sync on other devices, copy the "*set.toml*" file to the same directory as the Jar file on the other computer.
