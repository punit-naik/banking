#!/bin/bash

# Optional Steps to check if the JWT authentication is working properly
# Register the user
curl -X POST -v -d "user-name=punit-naik&password=test123"  http://localhost:3000/register-user

# Login to get the JWT
curl -X POST -v -d "user-name=punit-naik&password=test123"  http://localhost:3000/login

# API testing
# Create an account with a name (the API will return the created account information)
curl -X POST -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGRkMTYwNzM0MjJiNjc5MzMzZmQ0Y2VlYmI0N2E0ODlhJDEyJGY1YjI3N2FkMGRhMmI4MzQxNmNiYWMyODJhODM4NTE1M2JiZGMzMDgxMzRkYjc5NyJ9.2s6ULnOGUcdRcYF28c8Cxof1Qrj1AN1VWQ4uouzsQB8" -d "name=punit naik" http://localhost:3000/account
curl -X POST -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGRkMTYwNzM0MjJiNjc5MzMzZmQ0Y2VlYmI0N2E0ODlhJDEyJGY1YjI3N2FkMGRhMmI4MzQxNmNiYWMyODJhODM4NTE1M2JiZGMzMDgxMzRkYjc5NyJ9.2s6ULnOGUcdRcYF28c8Cxof1Qrj1AN1VWQ4uouzsQB8" -d "name=pingal naik" http://localhost:3000/account

# Get an account's information
curl -X GET -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGRkMTYwNzM0MjJiNjc5MzMzZmQ0Y2VlYmI0N2E0ODlhJDEyJGY1YjI3N2FkMGRhMmI4MzQxNmNiYWMyODJhODM4NTE1M2JiZGMzMDgxMzRkYjc5NyJ9.2s6ULnOGUcdRcYF28c8Cxof1Qrj1AN1VWQ4uouzsQB8" http://localhost:3000/account/1

# Deposit money into an account
curl -X POST -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGRkMTYwNzM0MjJiNjc5MzMzZmQ0Y2VlYmI0N2E0ODlhJDEyJGY1YjI3N2FkMGRhMmI4MzQxNmNiYWMyODJhODM4NTE1M2JiZGMzMDgxMzRkYjc5NyJ9.2s6ULnOGUcdRcYF28c8Cxof1Qrj1AN1VWQ4uouzsQB8" -d "amount=100" http://localhost:3000/account/1/deposit

# Withdraw money from an account
curl -X POST -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGRkMTYwNzM0MjJiNjc5MzMzZmQ0Y2VlYmI0N2E0ODlhJDEyJGY1YjI3N2FkMGRhMmI4MzQxNmNiYWMyODJhODM4NTE1M2JiZGMzMDgxMzRkYjc5NyJ9.2s6ULnOGUcdRcYF28c8Cxof1Qrj1AN1VWQ4uouzsQB8" -d "amount=20" http://localhost:3000/account/1/withdraw

# Send money from one account to another
curl -X POST -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGRkMTYwNzM0MjJiNjc5MzMzZmQ0Y2VlYmI0N2E0ODlhJDEyJGY1YjI3N2FkMGRhMmI4MzQxNmNiYWMyODJhODM4NTE1M2JiZGMzMDgxMzRkYjc5NyJ9.2s6ULnOGUcdRcYF28c8Cxof1Qrj1AN1VWQ4uouzsQB8" -d "amount=40&account_number=2" http://localhost:3000/account/1/send

# Get the audit logs of an account
curl -X GET -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGRkMTYwNzM0MjJiNjc5MzMzZmQ0Y2VlYmI0N2E0ODlhJDEyJGY1YjI3N2FkMGRhMmI4MzQxNmNiYWMyODJhODM4NTE1M2JiZGMzMDgxMzRkYjc5NyJ9.2s6ULnOGUcdRcYF28c8Cxof1Qrj1AN1VWQ4uouzsQB8" http://localhost:3000/account/1/audit
