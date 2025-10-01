from flask import Flask, render_template, request
from flask_socketio import SocketIO
from asyncua import Client
from asyncua import ua
import asyncio

'''
Ay bruh I spent a long time on this (like 3 hours) so this project better work.. capiche?
- YaBoiEkam
'''

# is_ready = False

app = Flask(__name__)
app.config['SECRET_KEY'] = '3K4MWasHereMONASHAUTOMATIONWOOOOOO'
socketio = SocketIO(app)

@app.route('/')
def main():
    return render_template('index.html')

@app.route('/sendOrder/', methods=['POST'])
def print_number():
    data = request.get_json()
    asyncio.run(sendOrder(data['actionNumber'], data['bottleOpen']))
    return 'Your order has been sent to the robot!'

url = "opc.tcp://172.24.200.1:4840"

async def sendOrder(val, bottle):
    print(f"Connecting to {url} ...")
    async with Client(url=url) as client:
        # Get the variable node using the path
        var = await client.nodes.root.get_child(["0:Objects", "21:robot1", "0:R1c_int1"])
        print(f"Variable Node: {var}")

        # Read the current value and data type
        # value = await var.read_value()
        # data_type = await var.read_data_type()
        # print(f"Current value of MyVariable ({var}): {value}")
        # print(f"Expected data type: {data_type}")

        # Set the new value back to the server
        new_value = ua.Variant(val, ua.VariantType.Int32)  # Ensure the value matches the expected Int32 type
        print(f"Setting value of MyVariable to {new_value} ...")
        await var.write_value(new_value)

        # Set int2 to bottle open or not
        var = await client.nodes.root.get_child(["0:Objects", "21:robot1", "0:R1c_int2"])
        new_value = ua.Variant(bottle, ua.VariantType.Int32)  # Ensure the value matches the expected Int32 type
        await var.write_value(new_value)

        print(f"Value successfully set to {new_value}.")

        var = await client.nodes.root.get_child(["0:Objects", "21:robot1", "0:R1c_Start"])
        await var.write_value(True)
        # Setting start to True
        print("Set R1c_Start to True")

if __name__ == "__main__":
    socketio.run(app, host='192.168.0.110', port=5000)
    # app.run()
    # asyncio.run(main())