const { createCanvas, loadImage } = require("canvas");
const fs = require("fs");

const filename = process.argv[2];

async function run() {
    const outPixelsOn = [];

    const image = await loadImage(filename);
    const c = createCanvas(36, 12);
    const ctx = c.getContext("2d");
    ctx.drawImage(image, 36 / 2 - image.width / 2, 0, image.width, 12);

    const data = ctx.getImageData(0, 0, 36, 12);
    for (let i = 0; i < data.data.length; i += 4) {
        const x = (i / 4) % 36;
        const y = Math.floor(i / 4 / 36);
        if (data.data[i] > 128) {
            outPixelsOn.push([x, y]);
        }
    }

    const outText = `${outPixelsOn.map((p) => p.join(",")).join(" ")}`;

    console.log(outText);

    //fs.writeFileSync(`a_badapple.txt`, outText + "\n");
}

run().then(() => {
    console.log("Done");
});
