const { createCanvas, loadImage } = require("canvas");
const fs = require("fs");

const filename = process.argv[2];

function colorToHex(r, g, b) {
    return `#${r.toString(16).padStart(2, "0")}${g.toString(16).padStart(2, "0")}${b.toString(16).padStart(2, "0")}`;
}

async function run() {
    let palette = {};

    const image = await loadImage(filename);
    const c = createCanvas(36, 12);
    const ctx = c.getContext("2d");
    ctx.drawImage(image, 36 / 2 - image.width / 2, 0, image.width, 12);

    const data = ctx.getImageData(0, 0, 36, 12);
    for (let i = 0; i < data.data.length; i += 4) {
        const c = colorToHex(data.data[i], data.data[i + 1], data.data[i + 2]);
        if (c == "#000000") continue;
        if (!palette[c]) {
            palette[c] = [];
        }
        const x = (i / 4) % 36;
        const y = Math.floor(i / 4 / 36);
        palette[c].push([x, y]);
    }

    const paletteKeys = Object.keys(palette);
    let outText = paletteKeys.join(" ");
    for (const k of paletteKeys) {
        outText += "|" + palette[k].map((p) => p.join(",")).join(" ");
    }

    console.log(outText);
}

run().then(() => {
    console.log("Done");
});
