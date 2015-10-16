# CommandTicks
CommandTicks is a Minecraft command & task executer. Tasks are scheduled using Unix Crontab syntax; when the time is right, the task's configured list of commands are executed in sequence.

Modifiers can be prepended to commands to alter their behaviour.
Currently, they are:

1. **^player** execute the command as each player online
2. **^pause <seconds>** wait <seconds> before executing command
3. **^weighted <chance>** only execute <chance>% of the time
4. **^say** colorized broadcast

# Todo:
1. Ability to chain command modifiers
2. Command placeholders, for example: %player%, %world%
3. API to add custom modifiers
