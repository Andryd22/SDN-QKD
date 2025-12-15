echo "Posting Key Managers on emulated qkd nodes..."
./post-ofc2024-nodes-emulated.curl.bash
sleep 2

echo "Posting Key Managers on PoliMi qkd nodes..."
./post-ofc2024-nodes-polimi.curl.bash
sleep 4

echo "Creating DIRECT QKD links..."
./post-ofc2024-links-direct-create.curl.bash
sleep 4

echo "Creating VIRTUAL QKD links..."
./post-ofc2024-links-virtual-create.curl.bash
sleep 2

echo "Activating QKD links"
./post-ofc2024-links-activate.curl.bash
