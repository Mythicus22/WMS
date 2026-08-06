import asyncio
import websockets

async def main():
    uri = "ws://127.0.0.1:8081/ws"

    async with websockets.connect(uri) as websocket:
        print("Connected!")

        while True:
            msg = await websocket.recv()
            print(msg)

asyncio.run(main())