# ACHE Ansible

This subdirectory contains [Ansible](https://www.ansible.com/) roles for
automatic configuration of the ACHE software stack.

## Dependecies

If you are setting up an Elasticsearch cluster, you will also need to clone
[ansible-elasticsearch](https://github.com/elastic/ansible-elasticsearch)
and make sure the symbolic link at `roles/elasticsearch` points to the
path where you cloned the `ansible-elasticsearch` role.

## Usage

#### Create an inventory file:

See the following inventory file for an example of expected variables:

```ini
ache1      ansible_ssh_host=127.0.0.1   public_ip=127.0.0.1   private_ip=192.168.0.1   ansible_ssh_private_key_file=./mykey.pem
elastic-1  ansible_ssh_host=127.0.0.10  public_ip=127.0.0.10  private_ip=192.168.0.11  ansible_ssh_private_key_file=./mykey.pem
elastic-2  ansible_ssh_host=127.0.0.11  public_ip=127.0.0.11  private_ip=192.168.0.12  ansible_ssh_private_key_file=./mykey.pem

[ache]
ache1

[es_node]
elastic-1
elastic-2

[es_node:vars]
es_cluster_name=my-cluster
es_heap_size=16G
```

#### Run it:

After creating a inventory file named `myinventory.cfg`, execute:

```bash
ansible-playbook -i myinventory.cfg ./setup-ache.yml
```
