import time
from flask import Flask, render_template
from asyncua import Client
from asyncua import ua
import asyncio

'''
Ay bruh I spent a long time on this (like 3 hours) so this project better work.. capiche?
- YaBoiEkam
'''


app = Flask(__name__)

@app.route('/')
def main():
    return render_template('index.html')

@app.route('/api/<int:number>', methods=['POST'])
def print_number(number):
    # print(f"Received number: {number}")
    asyncio.run(main(number))
    return f'You entered: {number}'

url = "opc.tcp://172.24.200.1:4840"

async def main(val):
    print(f"Connecting to {url} ...")
    async with Client(url=url) as client:

        # while True:
        #     var_ready = await client.nodes.root.get_child(["0:Objects", "21:robot1", "0:R1f_ready"])
        #     if var_ready == "true":
        #         break
        #     else:
        #         time.sleep(1)
        #         print(var_ready)
        # Get the variable nodes using the path
        var_int = await client.nodes.root.get_child(["0:Objects", "21:robot1", "0:R1c_int1"])
        var_start = await client.nodes.root.get_child(["0:Objects", "21:robot1", "0:R1c_Start"])

        # Read the current value and data type
        value = await var_int.read_value()
        data_type = await var_int.read_data_type()
        print(f"Variable Node: {var_int}")
        print(f"Current value of MyVariable ({var_int}): {value}")
        print(f"Expected data type: {data_type}")

        # Set the new value back to the server
        new_value = ua.Variant(val, ua.VariantType.Int32)  # Ensure the value matches the expected Int32 type
        print(f"Setting value of MyVariable to {new_value} ...")
        await var_int.write_value(new_value)

        print(f"Value successfully set to {new_value}.")

        # Turn on start
        await var_start.write_value(True)
        print(f"Enable successfully set to True")

if __name__ == "__main__":
    app.run()
    # asyncio.run(main())