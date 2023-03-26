@echo off

set GIT=git\\cmd\\git.exe
set PYTHON=py310\\python.exe

%PYTHON% web.py --precision fp16 --model-path "./model/chatglm-6b"

pause
exit /b