#/bin/bash

# sudo vi /etc/mail.rc
# set from=***@***.com smtp=smtp.***.com
# set smtp-auth-user=***@***.com smtp-auth-password=***
# set smtp-auth=login

echo "This is test mail." | mail -s "Test Mail" ***@**.com

